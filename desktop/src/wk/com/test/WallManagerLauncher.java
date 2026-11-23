package wk.com.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;
import com.badlogic.gdx.utils.NLog;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.FlowLayout;

import kw.manager.core.WindowManagerGame;
import kw.manager.core.listener.ManagerListener;
import wk.com.test.gameenum.StartEnum;
import wk.com.test.tray.TrayData;
import wk.com.test.tray.TrayItem;
import wk.com.test.tray.TrayItemManager;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/5 20:50
 */
public class WallManagerLauncher {
    private StartEnum startEnum;
    private volatile Process activeWallpaperProcess;
    private JLabel wallpaperStatus;

    public static void main(String[] args) {
        WallManagerLauncher wallManagerLauncher = new WallManagerLauncher();
        wallManagerLauncher.run();
    }

    public void run(){
        logConfig();
        this.startEnum = StartEnum.NEW_PROCESS;
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setDecorated(true);
        config.setResizable(false);
        Graphics.DisplayMode displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
        int screenWidth = displayMode.width;
        int screenHeight = displayMode.height;
        int windowWidth = screenWidth / 2;
        int windowHeight = screenHeight / 2;
        // 获取主显示器分辨率
        config.setWindowedMode(screenWidth/2, screenHeight/2);
        // 设置窗口居中
        int windowX = (screenWidth - windowWidth) / 2;
        int windowY = (screenHeight - windowHeight) / 2;
        config.setWindowPosition(windowX, windowY);
        config.setTitle( Config.APP_TITLE);
        Lwjgl3Application app = new Lwjgl3Application();
        config.setWindowListener(windowListener());
        SwingUtilities.invokeLater(this::showWallpaperControlPanel);
        Runtime.getRuntime().addShutdownHook(shutDown());
        // init() enters the LibGDX main loop and does not return until the app exits,
        // so desktop UI and shutdown hooks must be registered before this call.
        app.init(new WindowManagerGame(WindowManagerListener()), config);
    }

    private static void logConfig() {
        NLog.defaultTag = Config.APP_TITLE;
        NLog.i("set log and test!");
    }

    public Thread shutDown(){
        return new Thread(() -> {
            TrayItemManager.getTrayItemManager().closeTrayAndProcessAll();
            NLog.i("xxxxxxxxxxx","JVM 正在退出，执行清理逻辑");
            // 如果 LWJGL3Application 还在运行
            if (Gdx.app != null) {
                Gdx.app.exit();
            }
            System.out.println("清理完成，JVM退出");
            System.exit(0);
        });
    }

    public ManagerListener WindowManagerListener(){
        return new ManagerListener() {
            @Override
            public void setWallOrPet(int type) {
                setWallPaper(type);
            }

            @Override
            public void moveWindowPosition(float x, float y) {

            }
        };
    }

    private void setWallPaper(int type) {
        if (startEnum == StartEnum.NEW_PROCESS) {
            TrayItemManager trayItemManager = TrayItemManager.getTrayItemManager();
            trayItemManager.closeTrayAndProcessAll();
            TrayData trayData = new TrayData();
            trayItemManager.addTraydata(trayData);
            System.out.println("Original main start");
            // 构建命令，启动新的 JVM
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + "/bin/java";
            String classPath = System.getProperty("java.class.path");
            String className = "wk.com.test.WallpapgerLauncher";
            if (type == 0) {
                className = "wk.com.test.DesktopPetLauncher";
            }else {
                className = "wk.com.test.WallpapgerLauncher";
            }

            ProcessBuilder builder = new ProcessBuilder(
                    javaBin, "-cp", classPath, className
            );

//                    builder.inheritIO(); // 让新进程打印输出到当前控制台
            try {
                Process start = builder.start();
                activeWallpaperProcess = start;
                trayData.setProcess(start);
                TrayItem trayItem = new TrayItem();
                trayItem.showTray();
                trayData.setItem(trayItem);
                trayItem.setProcess(start);
                updateWallpaperStatus("壁纸运行中");
                watchWallpaperProcess(start);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Original main end");
        }else if (startEnum == StartEnum.NEW_PROCESS){
            WallpapgerLauncher.main(new String[]{});
        }else {

        }
    }

    private void showWallpaperControlPanel() {
        JFrame frame = new JFrame("壁纸控制面板");
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.setAlwaysOnTop(true);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        wallpaperStatus = new JLabel(activeWallpaperProcess == null ? "壁纸未启动" : "壁纸运行中");

        JButton pause = new JButton("暂停");
        pause.addActionListener(e -> sendWallpaperCommand("PAUSE"));
        JButton resume = new JButton("继续");
        resume.addActionListener(e -> sendWallpaperCommand("RESUME"));
        JCheckBox interaction = new JCheckBox("鼠标交互", true);
        interaction.addActionListener(e -> sendWallpaperCommand(
                interaction.isSelected() ? "INTERACTION_ON" : "INTERACTION_OFF"));
        JButton stop = new JButton("停止壁纸");
        stop.addActionListener(e -> {
            if (sendWallpaperCommand("EXIT")) {
                updateWallpaperStatus("正在停止...");
            }
        });
        JButton exit = new JButton("退出全部");
        exit.addActionListener(e -> System.exit(0));

        panel.add(wallpaperStatus);
        panel.add(pause);
        panel.add(resume);
        panel.add(interaction);
        panel.add(stop);
        panel.add(exit);
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private boolean sendWallpaperCommand(String command) {
        Process process = activeWallpaperProcess;
        if (process == null || !process.isAlive()) {
            updateWallpaperStatus("壁纸未启动，请先在主界面选择壁纸");
            return false;
        }
        try {
            OutputStream output = process.getOutputStream();
            output.write((command + "\n").getBytes(StandardCharsets.UTF_8));
            output.flush();
            if ("PAUSE".equals(command)) {
                updateWallpaperStatus("壁纸已暂停");
            } else if ("RESUME".equals(command)) {
                updateWallpaperStatus("壁纸运行中");
            }
            return true;
        } catch (IOException e) {
            updateWallpaperStatus("控制命令发送失败");
            NLog.i("wallpaper-control", "Failed to send command: " + e.getMessage());
            return false;
        }
    }

    private void watchWallpaperProcess(Process process) {
        Thread watcher = new Thread(() -> {
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                if (activeWallpaperProcess == process) {
                    activeWallpaperProcess = null;
                    updateWallpaperStatus("壁纸已停止");
                }
            }
        }, "wallpaper-process-watcher");
        watcher.setDaemon(true);
        watcher.start();
    }

    private void updateWallpaperStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            if (wallpaperStatus != null) {
                wallpaperStatus.setText(status);
            }
        });
    }

    public Lwjgl3WindowListener windowListener(){
        return new Lwjgl3WindowAdapter() {
            @Override
            public boolean closeRequested() {
                TrayItemManager.getTrayItemManager().closeTrayAndProcessAll();
                NLog.i("xxxxxxxxxxx","JVM 正在退出，执行清理逻辑");
                // 如果 LWJGL3Application 还在运行
                if (Gdx.app != null) {
                    Gdx.app.exit();
                }
                System.out.println("清理完成，JVM退出");
                System.exit(0);
                return true;
            }
        };
    }

}
