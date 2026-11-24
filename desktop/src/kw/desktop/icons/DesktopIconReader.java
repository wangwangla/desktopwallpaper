package kw.desktop.icons;

import com.sun.jna.*;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.*;

import kw.manager.core.DesktopIconSnapshot;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

/**
 * Reads Explorer's desktop ListView; all native/process-memory code stays in desktop.
 */
public final class DesktopIconReader implements AutoCloseable {
    @Structure.FieldOrder({"mask", "iItem", "iSubItem", "state", "stateMask", "pszText", "cchTextMax", "iImage", "lParam", "iIndent", "iGroupId", "cColumns", "puColumns", "piColFmt", "iGroup"})
    public static class ListItem extends Structure {
        public int mask, iItem;
        public Pointer pszText;
        public int cchTextMax;
        public Pointer lParam;
    }

    private interface Coordinates extends StdCallLibrary {
        Coordinates INSTANCE = Native.load("user32", Coordinates.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean ClientToScreen(WinDef.HWND window, WinDef.POINT point);
    }

    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "demo-desktop-icons");
        t.setDaemon(true);
        return t;
    });
    private volatile DesktopIconSnapshot snapshot = DesktopIconSnapshot.pending("正在读取桌面图标…");
    private WinDef.HWND listView;

    public DesktopIconSnapshot snapshot() {
        return snapshot;
    }

    public void start(WinDef.HWND wallpaper, BooleanSupplier active) {
        worker.scheduleWithFixedDelay(() -> {
            if (!active.getAsBoolean()) {
                snapshot = DesktopIconSnapshot.empty();
                return;
            }
            try {
                snapshot = read(wallpaper);
            } catch (Exception e) {
                var old = snapshot;
                snapshot = new DesktopIconSnapshot(
                        old.width(),
                        old.height(),
                        old.icons(),
                        false,
                        "桌面图标暂时不可读：" + e.getMessage());
            }
        }, 0, 180, TimeUnit.MILLISECONDS);
    }

    private static String className(WinDef.HWND window) {
        char[] name = new char[128];
        User32.INSTANCE.GetClassName(window, name, name.length);
        return Native.toString(name);
    }

    private WinDef.HWND findListView() {
        AtomicReference<WinDef.HWND> found = new AtomicReference<>();
        User32.INSTANCE.EnumWindows((parent, data) -> {
            if (!Set.of("Progman", "WorkerW").contains(className(parent))) return true;
            User32.INSTANCE.EnumChildWindows(parent, (child, p) -> {
                if (className(child).equals("SysListView32") && className(User32.INSTANCE.GetParent(child)).equals("SHELLDLL_DefView")) {
                    found.set(child);
                    return false;
                }
                return true;
            }, null);
            return found.get() == null;
        }, null);
        return found.get();
    }

    private static long message(WinDef.HWND window, int message, long w, long l) throws IOException {
        WinDef.DWORDByReference result = new WinDef.DWORDByReference();
        var sent = User32.INSTANCE.SendMessageTimeout(window, message, new WinDef.WPARAM(w), new WinDef.LPARAM(l), User32.SMTO_ABORTIFHUNG, 150, result);
        if (sent == null || sent.longValue() == 0) throw new IOException("Explorer 没有响应");
        return result.getValue().longValue();
    }

    private static void write(WinNT.HANDLE process, Pointer remote, Pointer local, int size) throws IOException {
        if (!Kernel32.INSTANCE.WriteProcessMemory(process, remote, local, size, null))
            throw new IOException("无法写入图标查询缓冲区");
    }

    private static void read(WinNT.HANDLE process, Pointer remote, Pointer local, int size) throws IOException {
        if (!Kernel32.INSTANCE.ReadProcessMemory(process, remote, local, size, null))
            throw new IOException("无法读取图标查询结果");
    }

    /**
     * Null wallpaper is supported for diagnostics and uses the primary desktop's physical pixels.
     */
    public DesktopIconSnapshot read(WinDef.HWND wallpaper) throws IOException {
        if (listView == null || !User32.INSTANCE.IsWindow(listView)) listView = findListView();
        if (listView == null) throw new IOException("没有找到桌面图标窗口");
        WinDef.RECT bounds = new WinDef.RECT();
        if (wallpaper != null) User32.INSTANCE.GetWindowRect(wallpaper, bounds);
        else {
            bounds.left = 0;
            bounds.top = 0;
            bounds.right = User32.INSTANCE.GetSystemMetrics(0);
            bounds.bottom = User32.INSTANCE.GetSystemMetrics(1);
        }
        int width = bounds.right - bounds.left, height = bounds.bottom - bounds.top;
        if (width <= 0 || height <= 0) throw new IOException("壁纸窗口尺寸无效");
        if (!User32.INSTANCE.IsWindowVisible(listView))
            return new DesktopIconSnapshot(width, height, List.of(), true, "桌面图标已隐藏");
        int count = (int) message(listView, 0x1004, 0, 0);
        if (count > 2048) throw new IOException("桌面图标数量异常");
        IntByReference pid = new IntByReference();
        User32.INSTANCE.GetWindowThreadProcessId(listView, pid);
        WinNT.HANDLE process = Kernel32.INSTANCE.OpenProcess(0x0008 | 0x0010 | 0x0020 | 0x1000, false, pid.getValue());
        if (process == null) throw new IOException("无法访问 Explorer 图标数据");
        Pointer remote = null;
        try {
            IntByReference wow64 = new IntByReference();
            Kernel32.INSTANCE.IsWow64Process(process, wow64);
            if (Native.POINTER_SIZE == 8 && wow64.getValue() != 0)
                throw new IOException("Explorer 与程序位数不同");
            remote = Kernel32.INSTANCE.VirtualAllocEx(process, null, new BaseTSD.SIZE_T(4096), 0x1000 | 0x2000, 0x04);
            if (remote == null) throw new IOException("无法分配图标查询缓冲区");
            WinDef.POINT origin = new WinDef.POINT();
            Coordinates.INSTANCE.ClientToScreen(listView, origin);
            List<DesktopIconSnapshot.Icon> icons = new ArrayList<>();
            Map<String, Integer> names = new HashMap<>();
            long started = System.nanoTime();
            for (int i = 0; i < count; i++) {
                if (Thread.currentThread().isInterrupted()) throw new IOException("图标读取已停止");
                if (System.nanoTime() - started > TimeUnit.MILLISECONDS.toNanos(600))
                    throw new IOException("图标查询超时");
                WinDef.RECT rect = new WinDef.RECT();
                rect.left = 0;
                rect.write();
                write(process, remote, rect.getPointer(), rect.size());
                if (message(listView, 0x100e, i, Pointer.nativeValue(remote)) == 0) continue;
                read(process, remote, rect.getPointer(), rect.size());
                rect.read();
                ListItem item = new ListItem();
                item.mask = 1 | 4;
                item.iItem = i;
                item.pszText = remote.share(256);
                item.cchTextMax = 512;
                item.write();
                write(process, remote, item.getPointer(), item.size());
                boolean hasParam = message(listView, 0x104b, 0, Pointer.nativeValue(remote)) != 0;
                if (!hasParam) message(listView, 0x1073, i, Pointer.nativeValue(remote));
                read(process, remote, item.getPointer(), item.size());
                item.read();
                Memory text = new Memory(1024);
                read(process, remote.share(256), text, 1024);
                String name = text.getWideString(0);
                int duplicate = names.merge(name, 1, Integer::sum);
                long identity = item.lParam == null ? 0 : Pointer.nativeValue(item.lParam);
                String id = hasParam && identity != 0 ? Long.toUnsignedString(identity, 16) : name + "#" + duplicate;
                int x = origin.x + rect.left - bounds.left, y = origin.y + rect.top - bounds.top, w = rect.right - rect.left, h = rect.bottom - rect.top;
                if (x + w > 0 && y + h > 0 && x < width && y < height)
                    icons.add(new DesktopIconSnapshot.Icon(id, name, x, y, w, h));
            }
            return new DesktopIconSnapshot(width, height, icons, true, "已读取桌面图标");
        } finally {
            if (remote != null)
                Kernel32.INSTANCE.VirtualFreeEx(process, remote, new BaseTSD.SIZE_T(0), 0x8000);
            Kernel32.INSTANCE.CloseHandle(process);
        }
    }

    @Override
    public void close() {
        worker.shutdownNow();
        try {
            worker.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
