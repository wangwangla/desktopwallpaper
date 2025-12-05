package wk.com.test.tray;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;

public class TrayItem {
    private TrayIcon trayIcon;
    public void showTray(){
        if (!SystemTray.isSupported()) {
            System.out.println("System tray not supported!");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage("icon.png"); // 图标路径

        trayIcon = new TrayIcon(image, "My App");
        trayIcon.setImageAutoSize(true); // 自动缩放图标
        trayIcon.setToolTip("这是我的应用程序");

        // 添加点击事件
        trayIcon.addActionListener(e -> {
            System.out.println("托盘图标被点击");
            // 可以显示窗口或者其他操作
        });

        // 创建菜单
        PopupMenu popup = new PopupMenu();
        MenuItem exitItem = new MenuItem("退出");
        exitItem.addActionListener(e -> {
            System.out.println("退出应用");
            System.exit(0);
        });
        popup.add(exitItem);
        trayIcon.setPopupMenu(popup);

        // 添加到系统托盘
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
        System.out.println("托盘图标添加成功");
    }

    public void close() {
        SystemTray tray = SystemTray.getSystemTray();
        tray.remove(trayIcon);
    }
}
