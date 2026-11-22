package kw.test;

import static com.sun.jna.platform.win32.WinUser.WS_CAPTION;
import static com.sun.jna.platform.win32.WinUser.WS_EX_COMPOSITED;
import static com.sun.jna.platform.win32.WinUser.WS_EX_LAYERED;
import static com.sun.jna.platform.win32.WinUser.WS_MAXIMIZEBOX;
import static com.sun.jna.platform.win32.WinUser.WS_MINIMIZEBOX;
import static com.sun.jna.platform.win32.WinUser.WS_SYSMENU;
import static com.sun.jna.platform.win32.WinUser.WS_THICKFRAME;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSize;
import static org.lwjgl.system.windows.User32.WS_EX_APPWINDOW;
import static org.lwjgl.system.windows.User32.WS_EX_CLIENTEDGE;
import static org.lwjgl.system.windows.User32.WS_EX_DLGMODALFRAME;
import static org.lwjgl.system.windows.User32.WS_EX_STATICEDGE;
import static org.lwjgl.system.windows.User32.WS_EX_TOOLWINDOW;
import static org.lwjgl.system.windows.User32.WS_EX_WINDOWEDGE;

import com.sun.jna.Pointer;
import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.glfw.GLFWVidMode;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import kw.demo.DisplayMonitorInfo;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/5 0:13
 */
public class DynamicUtils {

    private interface CoordinateApi extends StdCallLibrary {
        CoordinateApi INSTANCE = Native.load("user32", CoordinateApi.class, W32APIOptions.DEFAULT_OPTIONS);
        boolean ScreenToClient(WinDef.HWND window, WinDef.POINT point);
    }

    private static final int PROGMAN_SPAWN_WORKERW = 0x052C;
    private static final WinDef.HWND HWND_TOP = new WinDef.HWND(Pointer.createConstant(0));
    private static final Map<Long, WindowState> originalWindows = new HashMap<>();

    private static final class WindowState {
        int style, extendedStyle;
        WinDef.RECT rect = new WinDef.RECT();
    }

    private static final class DesktopLayer {
        WinDef.HWND parent;
        WinDef.HWND insertAfter = HWND_TOP;
        DesktopLayer(WinDef.HWND parent) { this.parent = parent; }
    }

    private static DesktopLayer getDesktopLayer() {
        WinDef.HWND progman = User32.INSTANCE.FindWindow("Progman", null);
        if (progman != null) {
            // Trigger WorkerW creation. Some Win11 builds need this call before EnumWindows can see it.
            User32.INSTANCE.SendMessageTimeout(progman, PROGMAN_SPAWN_WORKERW,
                    new WinDef.WPARAM(0), new WinDef.LPARAM(0), User32.SMTO_NORMAL, 1000, null);
            User32.INSTANCE.SendMessageTimeout(progman, PROGMAN_SPAWN_WORKERW,
                    new WinDef.WPARAM(0xD), new WinDef.LPARAM(0x1), User32.SMTO_NORMAL, 1000, null);
        }

        AtomicReference<WinDef.HWND> workerRef = new AtomicReference<>();
        AtomicReference<WinDef.HWND> defViewParentRef = new AtomicReference<>();
        AtomicReference<WinDef.HWND> defViewRef = new AtomicReference<>();

        User32.INSTANCE.EnumWindows((hWnd, data) -> {
            WinDef.HWND defView = User32.INSTANCE.FindWindowEx(hWnd, null, "SHELLDLL_DefView", null);
            if (defView == null || !User32.INSTANCE.IsWindowVisible(hWnd)) {
                return true;
            }

            // Keep the parent of SHELLDLL_DefView as fallback for Win11 variants.
            defViewParentRef.set(hWnd);
            defViewRef.set(defView);
            WinDef.HWND worker = User32.INSTANCE.FindWindowEx(null, hWnd, "WorkerW", null);
            while (worker != null && !User32.INSTANCE.IsWindowVisible(worker)) {
                worker = User32.INSTANCE.FindWindowEx(null, worker, "WorkerW", null);
            }
            if (worker != null) {
                workerRef.set(worker);
            }
            return false;
        }, null);

        WinDef.HWND iconsParent = defViewParentRef.get();
        if (iconsParent != null) {
            WinDef.HWND nestedWorker = User32.INSTANCE.FindWindowEx(iconsParent, null, "WorkerW", null);
            // Win11 24H2: icons and the system wallpaper are sibling children of Progman.
            // Insert our renderer immediately behind the transparent icon layer, ahead of WorkerW.
            if (nestedWorker != null && User32.INSTANCE.IsWindowVisible(nestedWorker)) {
                int iconStyle = User32.INSTANCE.GetWindowLong(defViewRef.get(), User32.GWL_EXSTYLE);
                if ((iconStyle & WS_EX_LAYERED) != 0) {
                    DesktopLayer layer = new DesktopLayer(iconsParent);
                    layer.insertAfter = defViewRef.get();
                    return layer;
                }
                return new DesktopLayer(nestedWorker);
            }
        }
        if (workerRef.get() != null) {
            return new DesktopLayer(workerRef.get());
        }
        if (iconsParent != null && (User32.INSTANCE.GetWindowLong(defViewRef.get(), User32.GWL_EXSTYLE) & WS_EX_LAYERED) != 0) {
            DesktopLayer layer = new DesktopLayer(iconsParent);
            layer.insertAfter = defViewRef.get();
            return layer;
        }
        throw new IllegalStateException("Could not find a visible desktop wallpaper layer");
    }

    public static void makeWallpaper(long window) {
        windowsMakeWallpaper(window);
    }

    public static void destroyWallpaper(long window) {
        windowsDestroyWallpaper(window);
    }

    private static void windowsMakeWallpaper(long window) {
        long nativeWindow = GLFWNativeWin32.glfwGetWin32Window(window);

        // procedure from https://github.com/Francesco149/weebp
        WinDef.HWND thisWindow = new WinDef.HWND(new Pointer(nativeWindow));


        if (originalWindows.containsKey(nativeWindow)) return;
        DesktopLayer layer = getDesktopLayer();
        WindowState original = new WindowState();
        original.style = User32.INSTANCE.GetWindowLong(thisWindow, User32.GWL_STYLE);
        original.extendedStyle = User32.INSTANCE.GetWindowLong(thisWindow, User32.GWL_EXSTYLE);
        User32.INSTANCE.GetWindowRect(thisWindow, original.rect);

        long style = User32.INSTANCE.GetWindowLong(thisWindow, User32.GWL_STYLE);
        style &= ~(WS_CAPTION | WS_THICKFRAME | WS_SYSMENU | WS_MAXIMIZEBOX | WS_MINIMIZEBOX);
        style |= User32.WS_CHILD;
        User32.INSTANCE.SetWindowLong(thisWindow, User32.GWL_STYLE, (int) style);

        // not sure if we need those, but better keep them in
        long exStyle = User32.INSTANCE.GetWindowLong(thisWindow, User32.GWL_EXSTYLE);
        exStyle &= ~(WS_EX_DLGMODALFRAME | WS_EX_COMPOSITED | WS_EX_WINDOWEDGE | WS_EX_CLIENTEDGE | WS_EX_LAYERED | WS_EX_STATICEDGE | WS_EX_TOOLWINDOW | WS_EX_APPWINDOW);
        User32.INSTANCE.SetWindowLong(thisWindow, User32.GWL_EXSTYLE, (int) exStyle);

        List<WinUser.MONITORINFOEX> monitors = DisplayMonitorInfo.getMonitors();
        if (monitors.size()>0) {
            WinUser.MONITORINFOEX monitor = monitors.get(0);
            for (WinUser.MONITORINFOEX candidate : monitors) if ((candidate.dwFlags & 1) != 0) monitor = candidate;
            User32.INSTANCE.SetParent(thisWindow, layer.parent);
            WinDef.HWND actualParent = User32.INSTANCE.GetParent(thisWindow);
            if (actualParent == null || !actualParent.getPointer().equals(layer.parent.getPointer())) {
                User32.INSTANCE.SetWindowLong(thisWindow, User32.GWL_STYLE, original.style);
                User32.INSTANCE.SetWindowLong(thisWindow, User32.GWL_EXSTYLE, original.extendedStyle);
                throw new IllegalStateException("Could not attach renderer to the desktop");
            }
            originalWindows.put(nativeWindow, original);

            GLFWVidMode glfwVidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            int width = glfwVidMode != null ? glfwVidMode.width() : monitor.rcMonitor.right - monitor.rcMonitor.left;
            int height = glfwVidMode != null ? glfwVidMode.height() : monitor.rcMonitor.bottom - monitor.rcMonitor.top;

            glfwSetWindowSize(window, width, height);
            WinDef.POINT origin = new WinDef.POINT(monitor.rcMonitor.left, monitor.rcMonitor.top);
            CoordinateApi.INSTANCE.ScreenToClient(layer.parent, origin);
            User32.INSTANCE.SetWindowPos(thisWindow, layer.insertAfter,
                    origin.x, origin.y, width, height,
                    User32.SWP_NOACTIVATE | User32.SWP_SHOWWINDOW | User32.SWP_FRAMECHANGED);
            User32.INSTANCE.ShowWindow(thisWindow, User32.SW_SHOW);
            System.out.printf("Wallpaper attached: parent=%x, after=%x, size=%dx%d%n",
                    Pointer.nativeValue(layer.parent.getPointer()), Pointer.nativeValue(layer.insertAfter.getPointer()), width, height);
        }
    }

    private static void windowsDestroyWallpaper(long window) {
        long nativeWindow = GLFWNativeWin32.glfwGetWin32Window(window);

        // procedure from https://github.com/Francesco149/weebp
        WinDef.HWND thisWindow = new WinDef.HWND(new Pointer(nativeWindow));

        WindowState original = originalWindows.remove(nativeWindow);
        if (original == null) return;
        User32.INSTANCE.SetParent(thisWindow, null);

        User32.INSTANCE.SetWindowLong(thisWindow, User32.GWL_STYLE, original.style);

        // not sure if we need those, but better keep them in
        User32.INSTANCE.SetWindowLong(thisWindow, User32.GWL_EXSTYLE, original.extendedStyle);
        User32.INSTANCE.SetWindowPos(thisWindow, HWND_TOP, original.rect.left, original.rect.top,
                original.rect.right - original.rect.left, original.rect.bottom - original.rect.top,
                User32.SWP_FRAMECHANGED | User32.SWP_SHOWWINDOW);
    }
}
