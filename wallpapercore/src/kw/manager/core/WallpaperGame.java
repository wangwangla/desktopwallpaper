package kw.manager.core;

import com.badlogic.gdx.Gdx;
import com.kw.gdx.BaseGame;
import com.kw.gdx.screen.BaseScreen;
import com.kw.gdx.constant.Constant;

/**
 * Shared Game lifecycle. Rendering belongs to Screens, platform operations to the callback.
 */
public abstract class WallpaperGame<T extends BaseScreen> extends BaseGame {
    private final WallpaperPlatform platform;
    private T wallpaperScreen;
    private boolean ready, closed;

    protected WallpaperGame(WallpaperPlatform platform) {
        this.platform = platform;
    }

    public final WallpaperPlatform platform() {
        return platform;
    }

    public final T wallpaperScreen() {
        return wallpaperScreen;
    }

    protected abstract T createScreen();


    @Override
    protected void initScreen() {
        loadingView();
    }

    @Override
    protected void loadingView() {
        wallpaperScreen = createScreen();
        setScreen(wallpaperScreen);
        // BaseGame changes its Screen on the render thread in the preceding queued runnable.
        Gdx.app.postRunnable(() -> {
            if (!closed) {
                ready = true;
                platform.ready(this);
            }
        });
    }

    @Override
    public void render() {
        super.render();
        if (ready && !closed) platform.frame(this);
    }

    @Override
    protected void preDiapose() {
        if (closed) return;
        closed = true;
        try {
            if (wallpaperScreen != null) wallpaperScreen.dispose();
        } finally {
            platform.disposed();
        }
    }
}
