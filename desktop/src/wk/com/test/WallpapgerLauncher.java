package wk.com.test;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.graphics.Color;
import com.wallper.listener.WindowListener;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.util.Locale;
import java.util.Scanner;

import kw.manager.core.WindowGame;
import kw.manager.core.listener.MoveListener;
import kw.test.DynamicUtils;
import wk.com.test.mouse.MouseHook;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/18 21:14
 */
class WallpapgerLauncher {
    private Lwjgl3Application app;
    private volatile boolean interactionEnabled = true;
    private volatile long wallpaperWindowHandle;
    private WindowGame windowGame;
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
        config.setDecorated(true);
        config.setResizable(false);
        // 获取当前屏幕尺寸
        Graphics.DisplayMode displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
        int screenWidth = displayMode.width;
        int screenHeight = displayMode.height;
        config.setWindowedMode(screenWidth, screenHeight);
        config.setWindowPosition(0, 0);
        // Configure window title
        final String TITLE = "A Day in Lines";
        config.setTitle(TITLE);
        config.setInitialVisible(true);
        config.setTransparentFramebuffer(false);
        config.setInitialBackgroundColor(new Color(0, 0, 0, 0));
        // Instantiate the App
        this.app = new Lwjgl3Application();
        config.setWindowListener(new Lwjgl3WindowAdapter(){
            @Override
            public boolean closeRequested() {
                MouseHook.uninstallHook();
                DynamicUtils.destroyWallpaper(app.getWindowHandle());
                System.out.println("Wallpaper cleaned.");
                System.exit(0);
                return super.closeRequested();
            }
        });
        Thread commandThread = new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (sc.hasNextLine()) {
                handleCommand(sc.nextLine());
            }
        }, "wallpaper-command-listener");
        commandThread.setDaemon(true);
        commandThread.start();

        WindowGame[] gameHolder = new WindowGame[1];
        windowGame = gameHolder[0] = new WindowGame(new WindowListener() {
            @Override
            public void windowForward() {
            }

            @Override
            public void moveWindowPosition(float x, float y) {

            }

            @Override
            public void setWallpaper() {
                long windowHandle = app.getWindowHandle();
                wallpaperWindowHandle = windowHandle;
                DynamicUtils.makeWallpaper(windowHandle);
                if (interactionEnabled) {
                    MouseHook.installHook(gameHolder[0], windowHandle);
                }
            }
        });
        //获取全局  鼠标点击

        app.setMouseMoveListener(new MoveListener() {
            @Override
            public void movePosition(float x, float y) {

            }

            @Override
            public void startPosition(float x, float y) {

            }
        });

        app.init(windowGame, config);
        return app.getWindowHandle();
    }

    private void handleCommand(String rawCommand) {
        String command = rawCommand == null ? "" : rawCommand.trim().toUpperCase(Locale.ROOT);
        switch (command) {
            case "PAUSE":
                Gdx.app.postRunnable(() -> Gdx.graphics.setContinuousRendering(false));
                break;
            case "RESUME":
                Gdx.app.postRunnable(() -> {
                    Gdx.graphics.setContinuousRendering(true);
                    Gdx.graphics.requestRendering();
                });
                break;
            case "INTERACTION_ON":
                interactionEnabled = true;
                if (wallpaperWindowHandle != 0 && windowGame != null) {
                    MouseHook.installHook(windowGame, wallpaperWindowHandle);
                }
                break;
            case "INTERACTION_OFF":
                interactionEnabled = false;
                MouseHook.uninstallHook();
                break;
            case "EXIT":
                MouseHook.uninstallHook();
                if (wallpaperWindowHandle != 0) {
                    DynamicUtils.destroyWallpaper(wallpaperWindowHandle);
                }
                System.out.println("Wallpaper cleaned.");
                System.exit(0);
                break;
            default:
                System.out.println("Unknown wallpaper command: " + rawCommand);
        }
    }
}
