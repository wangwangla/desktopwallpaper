package wk.com.test;

import kw.desktop.controls.DemoController;
import kw.manager.core.DemoGame;
import kw.wallpaper.DesktopLaunchOptions;
import kw.wallpaper.DesktopWallpaperHost;

public class DemoLauncher {
    public static void main(String[] args) {
        DesktopLaunchOptions options = new DesktopLaunchOptions(args);
        DesktopWallpaperHost host = new DesktopWallpaperHost(
                "Demo",
                1200,
                800,
                options.interactive(),
                options.wallpaper,
                options.quiet);
        host.run(options.window("Demo", 1200, 800, 60, true, false),
                platform -> {
            DemoGame game = new DemoGame(platform);
            return game;
        }, game -> {
                    DemoController controller = new DemoController(host);

        }, game -> {

        });
    }
}
