package wk.com.test;

import com.badlogic.gdx.Gdx;

import kw.manager.core.muyu.*;
import kw.desktop.controls.MuyuController;
import kw.wallpaper.*;

import javax.swing.SwingUtilities;

@WallpaperDefinition(id = "muyu", title = "一念木鱼", description = "轻敲木鱼，累计功德 · 作息与工作进度", accent = 0x9d6b45, thumbnail = "wallpaper-previews/muyu.png", sceneTitle = "一念木鱼 · LibGDX", controllerTitle = "一念木鱼 · 壁纸控制器", executables = {"Muyu.exe"})
public final class MuyuLauncher {
    public static void main(String[] args) {
        if (WallpaperSession.forwardExisting("muyu", args)) return;
        DesktopLaunchOptions options = new DesktopLaunchOptions(args);
        DesktopWallpaperHost host = new DesktopWallpaperHost("muyu", 1200, 800, options.interactive(), options.wallpaper, options.quiet);
        MuyuDesktopMouse mouse = new MuyuDesktopMouse();
        int[] frame = {0};
        host.run(options.window("一念木鱼 · LibGDX", 1200, 800, 60, true, false), platform -> {
            MuyuGame game = new MuyuGame(platform, options.interactive(), options.workTime);
            if (options.stroke) game.previewDelta = .02f;
            return game;
        }, game -> {
            if (options.interactive()) SwingUtilities.invokeLater(() -> {
                MuyuController controls = new MuyuController(((MuyuGame) game).wallpaperScreen(), host);
                host.controls(controls::show, controls::showWork, controls::dispose);
            });
        }, game -> {
            MuyuScreen screen = ((MuyuGame) game).wallpaperScreen();
            mouse.poll(screen, host.windowHandle(), host.isWallpaper());
            if (options.output == null) return;
            int n = ++frame[0];
            String name = null;
            if (options.stroke) {
                if (n == 12) name = "rest";
                if (n == 16) name = "raised";
                if (n == 20) name = "contact";
                if (n == 29) name = "returned";
            }
            if (name != null || (!options.stroke && n == 12))
                DesktopLaunchOptions.capture(options.stroke ? options.output.resolve(name + ".png") : options.output);
            if (options.stroke && n == 12) screen.hit();
            if (options.stroke ? n == 29 : n == 12) Gdx.app.exit();
        });
    }
}
