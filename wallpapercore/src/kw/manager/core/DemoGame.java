package kw.manager.core;

import com.badlogic.gdx.Screen;
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

    @Override
    public void desktopTouch(int x, int y, boolean pressed) {
        super.desktopTouch(x, y, pressed);
        Screen screen = getScreen();
        if (screen instanceof BaseScreen demo) {
            demo.desktopTouch(x, y, pressed);
        }
    }
}
