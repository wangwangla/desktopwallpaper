package kw.manager.core.seasons;

import kw.manager.core.*;

import com.kw.gdx.resource.annotation.GameInfo;

@GameInfo(width = 1600, height = 900)
public final class SeasonsGame extends WallpaperGame<SeasonsScreen> {
    public final SeasonSettings initialSettings;
    public final boolean interactive;

    public SeasonsGame(WallpaperPlatform platform, SeasonSettings settings, boolean interactive) {
        super(platform);
        this.initialSettings = settings.copy();
        this.interactive = interactive;
    }

    @Override
    protected SeasonsScreen createScreen() {
        return new SeasonsScreen(this);
    }
}
