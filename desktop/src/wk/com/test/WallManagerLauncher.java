package wk.com.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.NLog;

import java.io.IOException;

import kw.manager.core.WindowManagerGame;
import kw.manager.core.listener.ManagerListener;
import kw.test.DynamicUtils;
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
        config.setWindowedMode(500, 500);
        config.setWindowPosition(0, 100);
        config.setTitle( Config.APP_TITLE);
        Lwjgl3Application app = new Lwjgl3Application();
        config.setWindowListener(windowListener());
        app.init(new WindowManagerGame(WindowManagerListener()), config);
        Runtime.getRuntime().addShutdownHook(shutDown());
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
            public void windowForward() {
                if (startEnum == StartEnum.NEW_PROCESS) {
                    TrayItemManager trayItemManager = TrayItemManager.getTrayItemManager();
                    TrayData trayData = new TrayData();
                    trayItemManager.addTraydata(trayData);
                    System.out.println("Original main start");
                    // 构建命令，启动新的 JVM
                    String javaHome = System.getProperty("java.home");
                    String javaBin = javaHome + "/bin/java";
                    String classPath = System.getProperty("java.class.path");
                    String className = "wk.com.test.WallpapgerLauncher";
                    ProcessBuilder builder = new ProcessBuilder(
                            javaBin, "-cp", classPath, className
                    );

//                    builder.inheritIO(); // 让新进程打印输出到当前控制台
                    try {
                        Process start = builder.start();
                        trayData.setProcess(start);
                        TrayItem trayItem = new TrayItem();
                        trayItem.showTray();
                        trayData.setItem(trayItem);
                        trayItem.setProcess(start);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Original main end");
                }else {
                    WallpapgerLauncher.main(new String[]{});
                }
            }

            @Override
            public void moveWindowPosition(float x, float y) {

            }
        };
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
