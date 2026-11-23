package wk.com.test;

import com.badlogic.gdx.Gdx;

import kw.manager.core.seasons.*;
import kw.desktop.controls.SeasonController;
import kw.wallpaper.*;

import javax.swing.SwingUtilities;

@WallpaperDefinition(id = "seasons", title = "四季节气", description = "二十四节气流转 · 树木倒影与晴雨云雪", accent = 0x729579, thumbnail = "wallpaper-previews/seasons.png", sceneTitle = "四季节气 · 动态壁纸", controllerTitle = "四季节气 · 壁纸控制器", executables = {"SeasonsWallpaper.exe"})
public final class SeasonsLauncher {
    public static void main(String[] args) {
        if (WallpaperSession.forwardExisting("seasons", args)) return;
        DesktopLaunchOptions options = new DesktopLaunchOptions(args);
        DesktopWallpaperHost host = new DesktopWallpaperHost("seasons", 1280, 720, options.interactive(), options.wallpaper, options.quiet);
        SeasonSettings settings = options.interactive() ? SeasonSettings.loadSeason() : new SeasonSettings();
        int[] terms = {3, 9, 15, 20, 5, 12, 21, 17};
        String[] modes = {"clear", "clear", "cloudy", "snow", "rain", "clear", "fog", "overcast"};
        String[] names = {"spring-clear", "summer-clear", "autumn-cloudy", "winter-snow", "spring-rain", "autumn-night", "winter-fog", "autumn-overcast"};
        float[] temperatures = {18, 30, 21, -2, 18, 22, 3, 16};
        int[] counters = {0, 0};
        host.run(options.window("四季节气 · 动态壁纸", 1280, 720, 15, false, false), platform -> new SeasonsGame(platform, settings, options.interactive()), game -> {
            SeasonsScreen screen = ((SeasonsGame) game).wallpaperScreen();
            if (!options.interactive()) screen.preview(terms[0], 720, modes[0], temperatures[0]);
            else SwingUtilities.invokeLater(() -> {
                SeasonController controls = new SeasonController(screen::settings, screen::configure, screen.weather, host::wallpaper, host::preview, host::quit, false);
                host.controls(controls::show, controls::show, controls::dispose);
            });
        }, game -> {
            if (options.output == null || ++counters[0] < 3) return;
            DesktopLaunchOptions.capture(options.output.resolve(names[counters[1]] + ".png"));
            counters[0] = 0;
            if (++counters[1] == names.length) Gdx.app.exit();
            else {
                int i = counters[1];
                ((SeasonsGame) game).wallpaperScreen().preview(terms[i], i == 5 ? 1320 : 720, modes[i], temperatures[i]);
            }
        });
    }
}
