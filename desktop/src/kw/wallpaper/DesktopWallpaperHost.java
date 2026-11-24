package kw.wallpaper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.*;

import kw.manager.core.*;
import kw.manager.core.muyu.MuyuGame;
import kw.joker.DynamicUtils;

import org.lwjgl.glfw.GLFW;

import javax.swing.SwingUtilities;

import java.util.function.*;

/**
 * Desktop owns the window, native embedding, IPC, startup registration and Swing controls.
 */
public final class DesktopWallpaperHost implements WallpaperPlatform {
    private final String id;
    private final int width, height;
    private final boolean interactive, initialWallpaper, quiet;
    private Lwjgl3Application app;
    private WallpaperGame<?> game;
    private WallpaperSession session;
    private volatile boolean attached;
    private Runnable open = () -> {
    }, openWork = () -> {
    }, close = () -> {
    };
    private Consumer<WallpaperGame<?>> onReady = g -> {
    }, onFrame = g -> {
    };
    private Supplier<DesktopIconSnapshot> icons=DesktopIconSnapshot::empty;
    private Runnable release=()->{};
    public void releaseWith(Runnable cleanup){release=cleanup;}
    public void iconSource(Supplier<DesktopIconSnapshot> source){icons=source;}
    @Override public DesktopIconSnapshot desktopIcons(){return icons.get();}

    public DesktopWallpaperHost(String id, int width, int height, boolean interactive, boolean initialWallpaper, boolean quiet) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.interactive = interactive;
        this.initialWallpaper = initialWallpaper;
        this.quiet = quiet;
    }

    public void run(Lwjgl3ApplicationConfiguration configuration,
                    Function<WallpaperPlatform, WallpaperGame<?>> factory,
                    Consumer<WallpaperGame<?>> ready,
                    Consumer<WallpaperGame<?>> frame) {
        onReady = ready;
        onFrame = frame;
        app = new Lwjgl3Application();
        game = factory.apply(this);
        app.init(game, configuration);
        System.exit(0);
    }

    public void controls(Runnable open, Runnable work, Runnable close) {
        this.open = open;
        this.openWork = work;
        this.close = close;
        if (!quiet) open.run();
    }

    public long windowHandle() {
        return app.getWindowHandle();
    }

    @Override
    public boolean isWallpaper() {
        return attached;
    }

    @Override
    public void ready(WallpaperGame<?> game) {
        onReady.accept(game);
        if (interactive) try {
            session = new WallpaperSession(id, () -> attached ? "wallpaper" : "preview", Gdx.app::postRunnable, command -> {
                switch (command) {
                    case "wallpaper" -> wallpaper();
                    case "preview" -> preview();
                    case "controls" -> showControls();
                    case "stop" -> quit();
                }
            });
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
        if (initialWallpaper && interactive) Gdx.app.postRunnable(this::wallpaper);
    }

    @Override
    public void frame(WallpaperGame<?> game) {
        onFrame.accept(game);
    }

    @Override
    public void wallpaper() {
        if (!attached) {
            DynamicUtils.makeWallpaper(windowHandle());
            attached = true;
        }
        if (game instanceof MuyuGame muyu) muyu.setWallpaper(true);
        WallpaperAutostart.select(id);
    }

    @Override
    public void preview() {
        detach(true);
    }

    private void detach(boolean clearStartup) {
        if (attached) {
            DynamicUtils.destroyWallpaper(windowHandle());
            attached = false;
        }
        GLFW.glfwSetWindowSize(windowHandle(), width, height);
        GLFW.glfwSetWindowPos(windowHandle(), 60, 60);
        GLFW.glfwShowWindow(windowHandle());
        if (game instanceof MuyuGame muyu) muyu.setWallpaper(false);
        if (clearStartup) WallpaperAutostart.clear(id);
    }

    @Override
    public void quit() {
        if (attached) detach(true);
        else WallpaperAutostart.clear(id);
        Gdx.app.exit();
    }

    @Override
    public void showControls() {
        SwingUtilities.invokeLater(() -> open.run());
    }

    @Override
    public void showWorkControls() {
        SwingUtilities.invokeLater(() -> openWork.run());
    }

    @Override
    public void disposed() {
        if (session != null) session.close();
        release.run();
        if (attached) detach(false);
        SwingUtilities.invokeLater(() -> close.run());
    }
}
