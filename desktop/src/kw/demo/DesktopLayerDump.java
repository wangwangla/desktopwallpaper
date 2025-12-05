package kw.demo;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;

public class DesktopLayerDump {
    public static void main(String[] args) {
        final User32 user32 = User32.INSTANCE;
        WinDef.HWND desktop = user32.GetDesktopWindow();

        user32.EnumWindows((hWnd, data) -> {
            WinDef.HWND parent = user32.GetParent(hWnd);
            if (parent != null && parent.equals(desktop)) {
                char[] cls = new char[512];
                user32.GetClassName(hWnd, cls, 512);

                char[] title = new char[512];
                user32.GetWindowText(hWnd, title, 512);

                WinDef.RECT rect = new WinDef.RECT();
                user32.GetWindowRect(hWnd, rect);

                System.out.printf(
                        "[ROOT] HWND=%s | Class='%s' | Title='%s' | Rect=(%d,%d,%d,%d)%n",
                        hWnd, Native.toString(cls), Native.toString(title),
                        rect.left, rect.top, rect.right, rect.bottom
                );
            }
            return true;
        }, null);
    }

//    public static void main(String[] args) {
//        User32.INSTANCE.EnumWindows(new WNDENUMPROC() {
//            @Override
//            public boolean callback(HWND hWnd, Pointer data) {
//
//                // 只处理顶层窗口（没有 parent）
//                if (User32.INSTANCE.GetParent(hWnd) != null) {
//                    return true; // continue
//                }
//
//                // 是否可见 （可选）
//                if (!User32.INSTANCE.IsWindowVisible(hWnd)) {
//                    return true;
//                }
//
//                // 获取标题
//                char[] buffer = new char[512];
//                User32.INSTANCE.GetWindowText(hWnd, buffer, 512);
//                String title = Native.toString(buffer);
//
//                // 获取 class name
//                char[] className = new char[512];
//                User32.INSTANCE.GetClassName(hWnd, className, 512);
//                String cls = Native.toString(className);
//
//                // 获取窗口矩形
//                RECT rect = new RECT();
//                User32.INSTANCE.GetWindowRect(hWnd, rect);
//
//                // 过滤掉无效大小（可选）
//                int width = rect.right - rect.left;
//                int height = rect.bottom - rect.top;
//                if (width <= 0 || height <= 0) {
//                    return true;
//                }
//
//                System.out.printf(
//                        "[TopWindow] HWND=%s | Class='%s' | Title='%s' | Rect=(%d,%d,%d,%d)\n",
//                        hWnd,
//                        cls,
//                        title,
//                        rect.left, rect.top, rect.right, rect.bottom
//                );
//
//                return true;
//            }
//        }, null);
//    }
}
