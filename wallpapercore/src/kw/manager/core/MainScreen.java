package kw.manager.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import kw.manager.core.asset.Asset;
import kw.manager.core.base.BaseGame;
import kw.manager.core.base.BaseScreen;

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
        stage.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                System.out.println("--------------------------");
            }
        });
        Image image = new Image(Asset.getAsset().getTexture("white.png"));
        addActor(image);
        image.setColor(Color.ROYAL);
        image.setSize(Constant.GAMEWIDTH,Constant.GAMEHIGHT);
        image.setPosition(Constant.GAMEWIDTH/2.0f,Constant.GAMEHIGHT/2.0f, Align.center);
        image.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                image.setColor(Color.WHITE);
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                image.setColor(Color.RED);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                image.setColor(Color.ROYAL);
            }
        });
    }
}
