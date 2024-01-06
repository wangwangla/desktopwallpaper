package kw.demo;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/6 9:57
 */
public class Display {
    WinDef.HWND hWorkerW = new WinDef.HWND(Pointer.createConstant(0));

    public WinDef.HWND findDesktopWindow(WinDef.RECT rect) {

        User32 user32 = User32.INSTANCE;

        // 获取当前桌面窗口的父窗口句柄
        WinDef.HWND hProgMan = user32.FindWindow("Progman", "Program Manager");

        // 枚举所有子窗口，查找与工作区大小相同的窗口
        user32.EnumChildWindows(hProgMan, (hWnd, lParam) -> {
            WinDef.HWND hSHELLDLL_DefView = user32.FindWindowEx(hWnd, null, "SHELLDLL_DefView", null);
            if (hSHELLDLL_DefView != null) {
                WinDef.HWND hSysListView32 = user32.FindWindowEx(hSHELLDLL_DefView, null, "SysListView32", "FolderView");
                if (hSysListView32 != null) {
                    WinDef.RECT rect2 = new WinDef.RECT();
                    user32.GetWindowRect(hSysListView32, rect2);
                    if (rect2.equals(rect)) {
                        hWorkerW = hWnd;
                        return false;
                    }
                }
            }
            return true;
        }, null);

        // 返回找到的窗口句柄
        return hWorkerW;
    }
}
