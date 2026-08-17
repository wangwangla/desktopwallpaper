package wk.com.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.sun.jna.platform.win32.WinUser;
import com.wallper.listener.WindowListener;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import kw.demo.DisplayMonitorInfo;
import kw.manager.core.WindowGame;
import kw.manager.core.listener.MoveListener;
import kw.test.DynamicUtils;
import wk.com.test.mouse.MouseHook;

class WallpapgerLauncher {
    private Lwjgl3Application app;
    private List<WinUser.MONITORINFOEX> monitors;
    private final AtomicBoolean extraWindowsCreated = new AtomicBoolean(false);

    public static void main(String[] args) {
        new WallpapgerLauncher().run();
    }

    public static long setWall() {
        return new WallpapgerLauncher().run();
    }

    public long run() {
        monitors = DisplayMonitorInfo.getMonitors();
        if (monitors.isEmpty()) {
            throw new IllegalStateException("No display monitor found");
        }

        WinUser.MONITORINFOEX firstMonitor = monitors.get(0);
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setForegroundFPS(30);
        config.setIdleFPS(30);
        config.setDecorated(false);
        config.setResizable(false);
        config.setWindowedMode(monitorWidth(firstMonitor), monitorHeight(firstMonitor));
        config.setWindowPosition(firstMonitor.rcMonitor.left, firstMonitor.rcMonitor.top);
        config.setTitle("xx-0");
        config.setInitialVisible(true);
        config.setTransparentFramebuffer(false);
        config.setInitialBackgroundColor(new Color(0, 0, 0, 0));
        config.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override
            public boolean closeRequested() {
                closeWallpaper();
                return true;
            }
        });

        this.app = new Lwjgl3Application();
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                if ("EXIT".equals(scanner.nextLine())) {
                    closeWallpaper();
                }
            }
        }, "WallpaperConsole").start();

        app.setMouseMoveListener(new MoveListener() {
            @Override
            public void movePosition(float x, float y) {
            }

            @Override
            public void startPosition(float x, float y) {
            }
        });

        app.init(createWallpaperGame(firstMonitor, true), config);
        return app.getWindowHandle();
    }

    private WindowGame createWallpaperGame(WinUser.MONITORINFOEX monitor, boolean createOtherWindows) {
        WindowGame[] gameHolder = new WindowGame[1];
        gameHolder[0] = new WindowGame(new WindowListener() {
            @Override
            public void windowForward() {
            }

            @Override
            public void moveWindowPosition(float x, float y) {
            }

            @Override
            public void setWallpaper() {
                long handle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
                DynamicUtils.makeWallpaper(handle, monitor);
                if (createOtherWindows) {
                    MouseHook.installHook(gameHolder[0], handle);
                    createOtherMonitorWindows();
                }
            }
        });
        return gameHolder[0];
    }

    private void createOtherMonitorWindows() {
        if (!extraWindowsCreated.compareAndSet(false, true)) return;
        for (int i = 1; i < monitors.size(); i++) {
            WinUser.MONITORINFOEX monitor = monitors.get(i);
            Lwjgl3WindowConfiguration config = new Lwjgl3WindowConfiguration();
            config.setDecorated(false);
            config.setResizable(false);
            config.setWindowedMode(monitorWidth(monitor), monitorHeight(monitor));
            config.setWindowPosition(monitor.rcMonitor.left, monitor.rcMonitor.top);
            config.setTitle("xx-" + i);
            app.newWindow(createWallpaperGame(monitor, false), config);
        }
    }

    private static int monitorWidth(WinUser.MONITORINFOEX monitor) {
        return monitor.rcMonitor.right - monitor.rcMonitor.left;
    }

    private static int monitorHeight(WinUser.MONITORINFOEX monitor) {
        return monitor.rcMonitor.bottom - monitor.rcMonitor.top;
    }

    private void closeWallpaper() {
        MouseHook.uninstallHook();
        if (app != null) app.exit();
        System.out.println("Wallpaper cleaned.");
    }
}
