package wk.com.test.mouse;

import static org.lwjgl.system.windows.User32.WM_LBUTTONDOWN;

import com.badlogic.gdx.Gdx;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import org.lwjgl.glfw.GLFWNativeWin32;

import java.util.concurrent.atomic.AtomicBoolean;

import kw.manager.core.WindowGame;

/**
 * 此玩意很卡  gggg
 */
public class MouseHook {
    private interface DesktopUser32 extends StdCallLibrary {
        DesktopUser32 INSTANCE = com.sun.jna.Native.load(
                "user32", DesktopUser32.class, W32APIOptions.DEFAULT_OPTIONS);

        WinDef.HWND WindowFromPoint(long point);
    }

    private static volatile WinUser.HHOOK hHook;
    private static volatile int hookThreadId;
    private static volatile long wallpaperWindow;
    private static final AtomicBoolean running = new AtomicBoolean(false);

    private static final WinUser.LowLevelMouseProc mouseHook = new WinUser.LowLevelMouseProc() {
        @Override
        public WinDef.LRESULT callback(int nCode, WinDef.WPARAM wParam, WinUser.MSLLHOOKSTRUCT info) {
            if (nCode >= 0) {
                int msg = wParam.intValue();
                int x = info.pt.x;
                int y = info.pt.y;
                if (msg == WM_LBUTTONDOWN && isDesktopClick(x, y) && Gdx.app != null) {
                    WinDef.RECT rect = getWallpaperRect();
                    int localX = x - rect.left;
                    int localY = y - rect.top;
                    Gdx.app.postRunnable(() -> onMouseLeftDown(localX, localY));
                }
//                switch (msg) {
//                    case WM_LBUTTONDOWN:
//                        onMouseLeftDown(x, y);
//                        break;
//                    case WM_LBUTTONUP:
//                        onMouseLeftUp(x, y);
//                        break;
//                    case WM_RBUTTONDOWN:
//                        onMouseRightDown(x, y);
//                        break;
//                }
            }
            return User32.INSTANCE.CallNextHookEx(hHook, nCode, wParam, new WinDef.LPARAM());
        }
    };

    private static WindowGame windowGame;
    public static synchronized void installHook(WindowGame _windowGame, long windowHandle) {
        if (running.get()) return;
        windowGame = _windowGame;
        wallpaperWindow = GLFWNativeWin32.glfwGetWin32Window(windowHandle);
        running.set(true);
        Thread hookThread = new Thread(() -> {
            hookThreadId = Kernel32.INSTANCE.GetCurrentThreadId();
            hHook = User32.INSTANCE.SetWindowsHookEx(WinUser.WH_MOUSE_LL, mouseHook,
                    Kernel32.INSTANCE.GetModuleHandle(null), 0);
            if (hHook == null) {
                running.set(false);
                System.err.println("Failed to install mouse hook. Win32 error: "
                        + Kernel32.INSTANCE.GetLastError());
                return;
            }
            WinUser.MSG msg = new WinUser.MSG();
            while (running.get() && User32.INSTANCE.GetMessage(msg, null, 0, 0) > 0) {
                User32.INSTANCE.TranslateMessage(msg);
                User32.INSTANCE.DispatchMessage(msg);
            }
            User32.INSTANCE.UnhookWindowsHookEx(hHook);
            hHook = null;
        }, "WallpaperMouseHook");
        hookThread.setDaemon(true);
        hookThread.start();
    }

    public static synchronized void uninstallHook() {
        running.set(false);
        if (hookThreadId != 0) {
            User32.INSTANCE.PostThreadMessage(hookThreadId, WinUser.WM_QUIT,
                    new WinDef.WPARAM(), new WinDef.LPARAM());
        }
        hookThreadId = 0;
        wallpaperWindow = 0;
    }

    private static WinDef.RECT getWallpaperRect() {
        WinDef.RECT rect = new WinDef.RECT();
        WinDef.HWND hwnd = new WinDef.HWND(new com.sun.jna.Pointer(wallpaperWindow));
        User32.INSTANCE.GetWindowRect(hwnd, rect);
        return rect;
    }

    private static boolean isDesktopClick(int x, int y) {
        if (wallpaperWindow == 0) return false;
        WinDef.RECT rect = getWallpaperRect();
        if (x < rect.left || x >= rect.right || y < rect.top || y >= rect.bottom) {
            return false;
        }

        long point = (x & 0xffffffffL) | ((long) y << 32);
        WinDef.HWND hitWindow = DesktopUser32.INSTANCE.WindowFromPoint(point);
        while (hitWindow != null) {
            if (com.sun.jna.Pointer.nativeValue(hitWindow.getPointer()) == wallpaperWindow) {
                return true;
            }

            char[] className = new char[256];
            User32.INSTANCE.GetClassName(hitWindow, className, className.length);
            String name = com.sun.jna.Native.toString(className);
            if ("WorkerW".equals(name)
                    || "Progman".equals(name)
                    || "SHELLDLL_DefView".equals(name)
                    || "SysListView32".equals(name)) {
                return true;
            }
            hitWindow = User32.INSTANCE.GetParent(hitWindow);
        }
        return false;
    }

    private static void onMouseLeftDown(int x, int y) {
        System.out.println("LeftDown: " + x + ", " + y);
        windowGame.setMousePosition(x,y);
    }

    private static void onMouseLeftUp(int x, int y) {
        System.out.println("LeftUp: " + x + ", " + y);
    }

    private static void onMouseMove(int x, int y) {
//         System.out.println("Move: " + x + ", " + y);
    }

    private static void onMouseRightDown(int x, int y) {
        System.out.println("RightDown: " + x + ", " + y);
    }
}
