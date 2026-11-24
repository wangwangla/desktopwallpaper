package kw.manager.core;

import com.kw.gdx.BaseGame;
import com.kw.gdx.screen.BaseScreen;

import kw.manager.core.demo.DemoScreen;
import kw.manager.core.muyu.MuyuScreen;

public class DemoGame extends WallpaperGame<BaseScreen> {
    public DemoGame(WallpaperPlatform platform) {
        super(platform);
    }

    @Override
    protected BaseScreen createScreen() {
        return new DemoScreen(this);
    }

    @Override
    protected void loadingView() {
        super.loadingView();

    }
}
