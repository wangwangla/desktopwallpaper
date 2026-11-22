package wk.com.test;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.ScreenUtils;

import kw.manager.core.city.*;
import kw.test.DynamicUtils;

import org.lwjgl.glfw.GLFW;

import java.io.File;

public final class WeatherCityLauncher {
    private Lwjgl3Application app;
    private WeatherCityScreen scene;
    private boolean attached;

    public static void main(String[] args) {
        new WeatherCityLauncher().run(args);
    }

    private void run(String[] args) {
        String snapshot = null;
        boolean asWallpaper = false;
        for (int i = 0; i < args.length; i++) {
            if ("--snapshot".equals(args[i]) && i + 1 < args.length) snapshot = args[++i];
            if ("--wallpaper".equals(args[i])) asWallpaper = true;
        }
        final String output = snapshot;
        final boolean wallpaper = asWallpaper;
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("天气城市 · 写实风格预览");
        config.setWindowedMode(1280, 720);
        config.setResizable(true);
        config.setWindowPosition(40, 60);
        config.setBackBufferConfig(8, 8, 8, 8, 24, 0, 4);
        config.setForegroundFPS(output == null ? 30 : 60);
        config.setIdleFPS(output == null ? 30 : 60);
        config.disableAudio(true);
        config.setInitialVisible(output == null);
        app = new Lwjgl3Application();
        app.init(new ApplicationAdapter() {
            int frame, shot;
            final String[] names = {"day-clear", "dawn-cloudy", "day-rain", "night"};
            final String[] modes = {"clear", "cloudy", "rain", "clear"};
            final int[] minutes = {720, 390, 840, 1320};

            @Override
            public void create() {
                WeatherSettings settings = output == null ? WeatherSettings.load() : new WeatherSettings();
                if (output != null) {
                    settings.previewMinutes = minutes[0];
                    settings.weatherMode = modes[0];
                }
                scene = new WeatherCityScreen(settings, output == null ? new WeatherCityScreen.DesktopActions() {
                    public void wallpaper() {
                        attach();
                    }

                    public void preview() {
                        detach();
                    }

                    public void quit() {
                        if (attached) detach();
                        Gdx.app.exit();
                    }
                } : null, output == null);
                if (wallpaper) Gdx.app.postRunnable(WeatherCityLauncher.this::attach);
            }

            @Override
            public void render() {
                scene.render(Gdx.graphics.getDeltaTime());
                if (output != null && ++frame >= 12) {
                    Pixmap image = ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
                    File file = new File(output, names[shot] + ".png");
                    file.getParentFile().mkdirs();
                    PixmapIO.writePNG(new FileHandle(file), image, -1, true);
                    image.dispose();
                    System.out.println("Saved " + file);
                    shot++;
                    frame = 0;
                    if (shot >= names.length) Gdx.app.exit();
                    else scene.preview(minutes[shot], modes[shot]);
                }
            }

            @Override
            public void resize(int width, int height) {
                if (scene != null) scene.resize(width, height);
            }

            @Override
            public void dispose() {
                if (scene != null) scene.dispose();
            }
        }, config);
        // Swing's event thread must not keep a closed renderer alive.
        System.exit(0);
    }

    private void attach() {
        DynamicUtils.makeWallpaper(app.getWindowHandle());
        attached = true;
    }

    private void detach() {
        if (attached) {
            DynamicUtils.destroyWallpaper(app.getWindowHandle());
            attached = false;
        }
        GLFW.glfwSetWindowSize(app.getWindowHandle(), 1280, 720);
        GLFW.glfwSetWindowPos(app.getWindowHandle(), 80, 80);
        GLFW.glfwShowWindow(app.getWindowHandle());
    }
}
