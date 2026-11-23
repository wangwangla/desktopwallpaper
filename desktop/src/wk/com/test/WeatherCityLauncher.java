package wk.com.test;

import com.badlogic.gdx.Gdx;

import kw.manager.core.city.*;
import kw.desktop.controls.WeatherCityController;
import kw.wallpaper.*;

import javax.swing.SwingUtilities;

@WallpaperDefinition(id = "weather-city", title = "天气城市", description = "立体城市与光影 · 实时天气和昼夜变化", accent = 0x527e9d, thumbnail = "wallpaper-previews/weather-city.png", sceneTitle = "天气城市 · 写实风格预览", controllerTitle = "天气城市 · 控制器", executables = {"WeatherCity.exe"})
public final class WeatherCityLauncher {
    public static void main(String[] args) {
        if (WallpaperSession.forwardExisting("weather-city", args)) return;
        DesktopLaunchOptions options = new DesktopLaunchOptions(args);
        DesktopWallpaperHost host = new DesktopWallpaperHost("weather-city", 1280, 720, options.interactive(), options.wallpaper, options.quiet);
        WeatherSettings settings = options.interactive() ? WeatherSettings.load() : new WeatherSettings();
        if (!options.interactive()) {
            settings.previewMinutes = 720;
            settings.weatherMode = "clear";
        }
        String[] names = {"day-clear", "dawn-cloudy", "day-rain", "night"}, modes = {"clear", "cloudy", "rain", "clear"};
        int[] minutes = {720, 390, 840, 1320}, counters = {0, 0};
        host.run(options.window("天气城市 · 写实风格预览", 1280, 720, options.interactive() ? 30 : 60, false, true), platform -> new WeatherCityGame(platform, settings, options.interactive()), game -> {
            if (options.interactive()) SwingUtilities.invokeLater(() -> {
                WeatherCityController controls = new WeatherCityController(((WeatherCityGame) game).wallpaperScreen(), host);
                host.controls(controls::show, controls::show, controls::dispose);
            });
        }, game -> {
            if (options.output == null || ++counters[0] < 12) return;
            DesktopLaunchOptions.capture(options.output.resolve(names[counters[1]] + ".png"));
            counters[0] = 0;
            if (++counters[1] == names.length) Gdx.app.exit();
            else
                ((WeatherCityGame) game).wallpaperScreen().preview(minutes[counters[1]], modes[counters[1]]);
        });
    }
}
