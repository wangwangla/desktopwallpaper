package kw.learn.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import kw.learn.core.asset.Asset;
import kw.learn.core.base.BaseGame;
import kw.learn.core.base.BaseScreen;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/5 10:48
 */
public class MainScreen extends BaseScreen {

    public MainScreen(BaseGame game) {
        super(game);
    }

    @Override
    public void initView() {
        super.initView();
        Image image = new Image(Asset.getAsset().getTexture("white.png"));
        addActor(image);
        image.setColor(Color.ROYAL);
        image.setSize(500,500);
        image.setPosition(Constant.GAMEWIDTH/2.0f,Constant.GAMEHIGHT/2.0f, Align.center);
        image.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                System.out.println("---------------");
            }
        });
    }
}
