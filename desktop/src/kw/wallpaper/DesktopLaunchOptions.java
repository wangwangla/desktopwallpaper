package kw.wallpaper;

import java.nio.file.Path;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Command-line diagnostics and framebuffer files are desktop concerns.
 */
public final class DesktopLaunchOptions {
    public Path output;
    public boolean wallpaper, quiet, stroke;
    public java.time.LocalDateTime workTime;

    public DesktopLaunchOptions(String[] args) {
        for (int i = 0; i < args.length; i++)
            switch (args[i]) {
                case "--wallpaper" -> wallpaper = true;
                case "--quiet-controls" -> quiet = true;
                case "--snapshot" -> {
                    if (i + 1 < args.length) output = Path.of(args[++i]);
                }
                case "--stroke-snapshots" -> {
                    if (i + 1 < args.length) {
                        output = Path.of(args[++i]);
                        stroke = true;
                    }
                }
                case "--work-preview" -> {
                    if (i + 1 < args.length) workTime = java.time.LocalDateTime.parse(args[++i]);
                }
            }
    }

    public boolean interactive() {
        return output == null;
    }

    public Lwjgl3ApplicationConfiguration window(String title, int width, int height, int fps, boolean audio, boolean depth) {
        Lwjgl3ApplicationConfiguration c = new Lwjgl3ApplicationConfiguration();
        c.setTitle(title);
        c.setWindowedMode(width, height);
        c.setResizable(true);
        c.setWindowPosition(60, 60);
        c.setForegroundFPS(fps);
        c.setIdleFPS(fps);
        c.setInitialVisible(interactive() && !quiet);
        c.disableAudio(!audio || !interactive());
        c.setBackBufferConfig(8, 8, 8, 8, depth ? 24 : 0, 0, 4);
        return c;
    }

    public static void capture(Path file) {
        Pixmap image = ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        try {
            Path absolute = file.toAbsolutePath();
            java.nio.file.Files.createDirectories(absolute.getParent());
            PixmapIO.writePNG(new FileHandle(absolute.toFile()), image, -1, true);
            System.out.println("Saved " + absolute);
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        } finally {
            image.dispose();
        }
    }
}
