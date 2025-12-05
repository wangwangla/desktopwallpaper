package wk.com.test;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.graphics.Color;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.util.Scanner;

import kw.manager.core.WindowGame;
import kw.manager.core.listener.WindowListener;
import kw.test.DynamicUtils;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/18 21:14
 */
class WallpapgerLauncher {
    private Lwjgl3Application app;
    public static void main(String[] args) {
        WallpapgerLauncher launcher = new WallpapgerLauncher();
        launcher.run();
    }


    public static long setWall(){
        WallpapgerLauncher launcher = new WallpapgerLauncher();
        return launcher.run();
    }

    public long run(){

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        // Configure FPS
        config.setForegroundFPS(30);
        config.setIdleFPS(30);
        // Configure window layout
        config.setDecorated(false);
        config.setResizable(false);
        config.setWindowedMode((int) (1920 * 0.5f), (int) (1200 * 0.5f));
        config.setWindowPosition(0, 100);
        // Configure window title
        final String TITLE = "xx";
        config.setTitle(TITLE);
        config.setInitialVisible(true);
        config.setTransparentFramebuffer(true);
        config.setInitialBackgroundColor(new Color(0, 0, 0, 0));
        config.setWindowListener(new Lwjgl3WindowAdapter(){
            @Override
            public boolean closeRequested() {
                System.out.println("---------------dddddddddddddddddddddddddddd");
                DynamicUtils.destroyWallpaper(app.getWindowHandle());
                return super.closeRequested();
            }
        });
        // Instantiate the App
        this.app = new Lwjgl3Application();

        new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (sc.hasNextLine()) {
                System.out.println(sc.hasNextLine());
                if (sc.nextLine().equals("EXIT")) {
                    DynamicUtils.destroyWallpaper(app.getWindowHandle());
                    System.out.println("Wallpaper cleaned.");
                    System.exit(0);
                }
            }
        }).start();

        app.init(new WindowGame(new WindowListener() {
            @Override
            public void windowForward() {
//                long windowHandle = app.getWindowHandle();
//                GLFW.glfwSetWindowAttrib(windowHandle,GLFW.GLFW_FLOATING,GLFW.GLFW_TRUE);
            }

            @Override
            public void moveWindowPosition(float x, float y) {
//                long windowHandle = app.getWindowHandle();
//                int windowX[] = new int[1];
//                int windowY[] = new int[1];
//                GLFW.glfwGetWindowPos(windowHandle, windowX, windowY);
//                GLFW.glfwFocusWindow(windowHandle);
            }

            @Override
            public void setWallpaper() {
                long windowHandle = app.getWindowHandle();
                DynamicUtils.makeWallpaper(windowHandle);
                System.out.println("-0-0-0-0-0-0-0-0-0-0");
            }
        }), config);

        System.out.println("00000000000000000000000sssssssssssssss");
        return app.getWindowHandle();
    }
}
