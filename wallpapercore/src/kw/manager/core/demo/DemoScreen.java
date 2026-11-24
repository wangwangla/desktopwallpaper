package kw.manager.core.demo;

import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Scaling;
import com.kw.gdx.screen.BaseScreen;

import kw.manager.core.*;

public class DemoScreen extends BaseScreen {
    private final DemoGame demo;
    private final WallpaperAssets assets = new WallpaperAssets();
    private final Image background = assets.image("muyu/background.png");
    private final IconBackdropLayer icons = new IconBackdropLayer(() -> {
        Image image = assets.image("ui/rounded.png");
        image.setColor(.27f, .47f, .76f, .32f);
        return image;
    });
    private final Label status = assets.label("设置为壁纸后，背景会跟随桌面图标", 15, 0x53667b);
    private final Group content = new Group() {
        @Override
        public void act(float delta) {
            var viewport = stage.getViewport();
            float w = viewport.getWorldWidth(), h = viewport.getWorldHeight();
            background.setSize(w, h);
            DesktopIconSnapshot snapshot = demo.platform().desktopIcons();
            icons.synchronize(snapshot, w, h);
            status.setText(snapshot.available() ? "图标背景 · " + icons.iconCount() + " 个 · 移动后缓动跟随" : snapshot.status());
            status.pack();
            status.setPosition(Math.max(25, w - status.getWidth() - 30), 24);
            super.act(delta);
        }
    };

    public DemoScreen(DemoGame game) {
        super(game);
        demo = game;
        background.setScaling(Scaling.fill);
    }

    @Override
    public void initView() {
        if (content.getStage() != null) return;
        content.addActor(background);
        content.addActor(icons);
        content.addActor(status);
        stage.addActor(content);
    }

    public int backgroundCount() {
        return icons.iconCount();
    }

    @Override
    public void dispose() {
        super.dispose();
        assets.close();
    }
}
