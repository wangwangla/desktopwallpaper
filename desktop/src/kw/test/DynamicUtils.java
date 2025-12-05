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
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.glfw.GLFWVidMode;

import java.util.concurrent.atomic.AtomicReference;

import kw.demo.DisplayMonitorInfo;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/5 0:13
 */
public class DynamicUtils {

    private static WinDef.HWND getWorkerW() {
        return getWorkerW(null);
    }

    private static WinDef.HWND getWorkerW(WinDef.RECT rect) {
        WinDef.HWND progman = User32.INSTANCE.FindWindow("Progman", null);

        User32.INSTANCE.SendMessageTimeout(progman, 0x052C, new WinDef.WPARAM(0xD), new WinDef.LPARAM(0x1), User32.SMTO_NORMAL, 1000, null);
//        User32.INSTANCE.SendMessage(progman, 0x052C, new WinDef.WPARAM(0xD), new WinDef.LPARAM(1));

        AtomicReference<WinDef.HWND> workerRef = new AtomicReference<>();
        User32.INSTANCE.EnumWindows(new WinUser.WNDENUMPROC() {
            @Override
            public boolean callback(WinDef.HWND hWnd, Pointer data) {
                if (User32.INSTANCE.FindWindowEx(hWnd, null, "SHELLDLL_DefView", null) == null)
                    return true;

                WinDef.HWND worker = User32.INSTANCE.FindWindowEx(null, hWnd, "WorkerW", null);
                if (worker != null) {

                    WinDef.RECT rect2 = new WinDef.RECT();
                    User32.INSTANCE.GetClientRect(worker, rect2);
                    System.out.println(rect2 + "  " + rect + "===" + worker);
                    workerRef.set(worker);
                }
                return true;

            }
        }, null);
        return workerRef.get();
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


        WinDef.RECT rect = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(thisWindow, rect);

        long style = User32.INSTANCE.GetWindowLong(thisWindow, User32.GWL_STYLE);
        style &= ~(WS_CAPTION | WS_THICKFRAME | WS_SYSMENU | WS_MAXIMIZEBOX | WS_MINIMIZEBOX);
        style |= User32.WS_CHILD;
        User32.INSTANCE.SetWindowLong(thisWindow, User32.GWL_STYLE, (int) style);

        // not sure if we need those, but better keep them in
        long exStyle = User32.INSTANCE.GetWindowLong(thisWindow, User32.GWL_EXSTYLE);
        exStyle &= ~(WS_EX_DLGMODALFRAME | WS_EX_COMPOSITED | WS_EX_WINDOWEDGE | WS_EX_CLIENTEDGE | WS_EX_LAYERED | WS_EX_STATICEDGE | WS_EX_TOOLWINDOW | WS_EX_APPWINDOW);
        User32.INSTANCE.SetWindowLong(thisWindow, User32.GWL_EXSTYLE, (int) exStyle);

        for (WinUser.MONITORINFOEX monitor : DisplayMonitorInfo.getMonitors()) {
            WinDef.HWND desktopWindow = getWorkerW(monitor.rcMonitor);

            User32.INSTANCE.SetParent(thisWindow, desktopWindow);
            User32.INSTANCE.ShowWindow(thisWindow, User32.SW_SHOW);
            GLFWVidMode glfwVidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());

            WinDef.RECT rect2 = new WinDef.RECT();
            User32.INSTANCE.GetClientRect(desktopWindow, rect2);

            int width = glfwVidMode.width();
            int height = glfwVidMode.height();
            //根据窗口多少来确定
            System.out.println(monitor.rcMonitor.left + "   " + monitor.rcMonitor.top);
//            glfwSetWindowPos(window, monitor.rcMonitor.left,monitor.rcMonitor.top);
            glfwSetWindowPos(window, 0, 0);

            glfwSetWindowSize(window, width, height);
        }

    }

    private static void windowsDestroyWallpaper(long window) {
        long nativeWindow = GLFWNativeWin32.glfwGetWin32Window(window);

        // procedure from https://github.com/Francesco149/weebp
        WinDef.HWND thisWindow = new WinDef.HWND(new Pointer(nativeWindow));

        User32.INSTANCE.SetParent(thisWindow, User32.INSTANCE.GetDesktopWindow());

        long style = User32.INSTANCE.GetWindowLong(thisWindow, User32.GWL_STYLE);
        style |= User32.WS_OVERLAPPEDWINDOW;
        User32.INSTANCE.SetWindowLong(thisWindow, User32.GWL_STYLE, (int) style);

        // not sure if we need those, but better keep them in
        long exStyle = User32.INSTANCE.GetWindowLong(thisWindow, User32.GWL_EXSTYLE);
        exStyle |= WS_EX_APPWINDOW;
        User32.INSTANCE.SetWindowLong(thisWindow, User32.GWL_EXSTYLE, (int) exStyle);

        getWorkerW();
    }
}