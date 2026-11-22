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
}