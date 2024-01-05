package kw.manager.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import kw.manager.core.asset.ManagerAsset;
import kw.manager.core.base.ManagerBaseGame;
import kw.manager.core.base.ManagerBaseScreen;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/5 10:48
 */
public class ManagerMainScreen extends ManagerBaseScreen {

    public ManagerMainScreen(ManagerBaseGame game) {
        super(game);
    }

    @Override
    public void initView() {
        super.initView();
        Image bg = new Image(ManagerAsset.getAsset().getTexture("white.png"));
        addActor(bg);
        bg.setColor(Color.ROYAL);
        bg.setSize(ManagerConstant.GAMEWIDTH,ManagerConstant.GAMEHIGHT);
        bg.setPosition(ManagerConstant.GAMEWIDTH/2.0f, ManagerConstant.GAMEHIGHT/2.0f, Align.center);
        bg.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                System.out.println("---------------");
            }
        });
    }
}
