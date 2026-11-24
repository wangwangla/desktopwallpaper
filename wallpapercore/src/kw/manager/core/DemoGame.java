package kw.manager.core;

import com.kw.gdx.BaseGame;
import com.kw.gdx.screen.BaseScreen;

import kw.manager.core.demo.DemoScreen;
import com.kw.gdx.resource.annotation.GameInfo;

@GameInfo(width=1200,height=800)
public class DemoGame extends WallpaperGame<DemoScreen> {
    public DemoGame(WallpaperPlatform platform) {
        super(platform);
    }

    @Override
    protected DemoScreen createScreen() {
        return new DemoScreen(this);
    }

    @Override
    protected void loadingView() {
        super.loadingView();

    }
}
