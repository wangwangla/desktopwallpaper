package wk.com.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;
import com.badlogic.gdx.graphics.Color;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.IOException;

import kw.manager.core.WindowManagerGame;
import kw.manager.core.listener.ManagerListener;
import wk.com.test.gameenum.StartEnum;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/5 20:50
 */
public class WallManagerLauncher {
    private static StartEnum startEnum = StartEnum.NEW_PROCESS;

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        // Configure FPS
        config.setDecorated(true);
        config.setResizable(false);
        config.setWindowedMode(500, 500);
        config.setWindowPosition(0, 100);
        // Configure window title
        final String TITLE = "xxxxxxxxx";
        config.setTitle(TITLE);
        config.setInitialVisible(true);
        config.setTransparentFramebuffer(true);
        config.setInitialBackgroundColor(new Color(0, 0, 0, 0));
        // Instantiate the App
        Lwjgl3Application app = new Lwjgl3Application();

        config.setWindowListener(new Lwjgl3WindowListener() {
            @Override
            public void created(Lwjgl3Window window) {

            }

            @Override
            public void iconified(boolean isIconified) {

            }

            @Override
            public void maximized(boolean isMaximized) {

            }

            @Override
            public void focusLost() {

            }

            @Override
            public void focusGained() {

            }

            @Override
            public boolean closeRequested() {
                if (startEnum == StartEnum.NEW_PROCESS) {

                    System.out.println("窗口关闭请求");
                    // 关闭应用
                    Gdx.app.exit(); // 结束 Lwjgl3Application 渲染线程
                    System.exit(0); // 如果你希望完全退出 JVM
                    return true;
                }else {
                    System.out.println("窗口关闭请求");
                    // 关闭应用
                    Gdx.app.exit(); // 结束 Lwjgl3Application 渲染线程
                    System.exit(0); // 如果你希望完全退出 JVM
                    return true;
                }
            }

            @Override
            public void filesDropped(String[] files) {

            }

            @Override
            public void refreshRequested() {

            }
        });

        app.init(new WindowManagerGame(new ManagerListener() {
            @Override
            public void windowForward() {
                if (startEnum == StartEnum.NEW_PROCESS) {
                    System.out.println("Original main start");
                    // 构建命令，启动新的 JVM
                    String javaHome = System.getProperty("java.home");
                    String javaBin = javaHome + "/bin/java";
                    String classPath = System.getProperty("java.class.path");
                    String className = "wk.com.test.WallpapgerLauncher";

                    ProcessBuilder builder = new ProcessBuilder(
                            javaBin, "-cp", classPath, className
                    );
                    builder.inheritIO(); // 让新进程打印输出到当前控制台
                    try {
                        builder.start();
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
        }), config);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("JVM 正在退出，执行清理逻辑");
            // 如果 LWJGL3Application 还在运行
            if (Gdx.app != null) {
                Gdx.app.exit();
            }

            System.out.println("清理完成，JVM退出");
        }));
    }

}
