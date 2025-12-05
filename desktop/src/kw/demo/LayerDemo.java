package kw.demo;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeWin32;

public class LayerDemo {

    public static void main(String[] args) {
        // 初始化 GLFW
        if (!GLFW.glfwInit()) {
            System.err.println("GLFW init failed");
            return;
        }

        long window = GLFW.glfwCreateWindow(800, 600, "Test Wallpaper", 0, 0);
        if (window == 0) {
            System.err.println("Failed to create GLFW window");
            return;
        }

        // 获取 Win32 窗口句柄
        long hwndLong = GLFWNativeWin32.glfwGetWin32Window(window);
        WinDef.HWND hwnd = new WinDef.HWND(new Pointer(hwndLong));

        // 获取 Win11 桌面图标父窗口
        WinDef.HWND desktop = getWin11Desktop();
        System.out.println("Desktop HWND: " + desktop);

        // 设置父窗口
        User32.INSTANCE.SetParent(hwnd, desktop);

        // 打印窗口 Z 顺序
        System.out.println("Wallpaper HWND: " + hwnd);
        System.out.println("SetParent done. Now check if icons are above this window.");
        WinDef.HWND HWND_BOTTOM = new WinDef.HWND(Pointer.createConstant(1));
        // 放到最底层，保证图标在上面
        User32.INSTANCE.SetWindowPos(hwnd, HWND_BOTTOM,
                0, 0, 800, 600,
                User32.SWP_NOACTIVATE | User32.SWP_SHOWWINDOW);

        // 运行一个简单循环，保持窗口
        while (!GLFW.glfwWindowShouldClose(window)) {
            GLFW.glfwPollEvents();
        }

        GLFW.glfwTerminate();
    }

    private static WinDef.HWND getWin11Desktop() {
        final WinDef.HWND[] desktop = {null};

        User32.INSTANCE.EnumWindows((hWnd, data) -> {
            WinDef.HWND defView = User32.INSTANCE.FindWindowEx(hWnd, null, "SHELLDLL_DefView", null);
            if (defView != null) {
                desktop[0] = hWnd; // Win11 桌面图标父窗口
                return false;
            }
            return true;
        }, null);

        return desktop[0];
    }
}
