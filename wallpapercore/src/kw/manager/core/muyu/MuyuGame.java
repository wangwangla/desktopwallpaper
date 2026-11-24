package kw.manager.core.muyu;

import kw.manager.core.*;

import com.kw.gdx.resource.annotation.GameInfo;

@GameInfo(width = 1200, height = 800)
public final class MuyuGame extends WallpaperGame<MuyuScreen> {
    public final boolean interactive;
    public float previewDelta = -1;
    private final java.time.LocalDateTime workPreview;

    public MuyuGame(WallpaperPlatform platform, boolean interactive, java.time.LocalDateTime workPreview) {
        super(platform);
        this.interactive = interactive;
        this.workPreview = workPreview;
    }

    @Override
    protected void gameInfoConfig() {
        super.gameInfoConfig();
        com.kw.gdx.constant.Constant.viewColor.set(.96f, .94f, .9f, 1);
    }

    @Override
    protected MuyuScreen createScreen() {
        MuyuScreen screen = new MuyuScreen(this);
        if (workPreview != null) screen.previewWorkTime(workPreview);
        return screen;
    }

    public void setWallpaper(boolean value) {
        if (wallpaperScreen() != null) wallpaperScreen().setWallpaper(value);
    }
}
