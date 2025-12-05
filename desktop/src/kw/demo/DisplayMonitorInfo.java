package kw.demo;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取两个屏幕
 * <p>
 * 打印信息， 并获取句柄
 *
 * @Auther jian xian si qi
 * @Date 2024/1/5 23:25
 */
public class DisplayMonitorInfo {

    public static void main(String[] args) {
        List<WinUser.MONITORINFOEX> monitors = getMonitors();
        for (WinUser.MONITORINFOEX monitor : monitors) {
            System.out.println("Monitor " + new String(monitor.szDevice) + ":");
            System.out.println("\tLeft: " + monitor.rcMonitor.left);
            System.out.println("\tTop: " + monitor.rcMonitor.top);
            System.out.println("\tRight: " + monitor.rcMonitor.right);
            System.out.println("\tBottom: " + monitor.rcMonitor.bottom);
            // 获取第一个显示器的句柄
            WinDef.POINT.ByValue byValue = new WinDef.POINT.ByValue(monitor.rcMonitor.left, monitor.rcMonitor.top);
            WinUser.HMONITOR hMonitor = User32.INSTANCE.MonitorFromPoint(
                    byValue,
                    WinUser.MONITOR_DEFAULTTONEAREST);
            // 输出显示器句柄
            System.out.println("显示器句柄: " + hMonitor);
        }
    }

    public static List<WinUser.MONITORINFOEX> getMonitors() {
        final List<WinUser.MONITORINFOEX> monitors = new ArrayList<>();
        User32.INSTANCE.EnumDisplayMonitors(null, null, new User32.MONITORENUMPROC() {
            @Override
            public int apply(WinUser.HMONITOR hMonitor, WinDef.HDC hdcMonitor, WinDef.RECT lprcMonitor, WinDef.LPARAM dwData) {
                WinUser.MONITORINFOEX monitorInfo = new WinUser.MONITORINFOEX();
                User32.INSTANCE.GetMonitorInfo(hMonitor, monitorInfo);
                monitors.add(monitorInfo);
                return 1;
            }
        }, null);
        return monitors;
    }
}
