package kw.manager.core;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import kw.manager.core.asset.Asset;
import kw.manager.core.base.BasePetGame;
import kw.manager.core.base.BasePetScreen;
import kw.manager.core.constant.Constant;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/5 10:48
 */
public class PetScreen extends BasePetScreen {
    public PetScreen(BasePetGame game) {
        super(game);
    }

    @Override
    public void initView() {
        super.initView();

        Image image = new Image(Asset.getAsset().getTexture("resource/zhuochong/img.png"));
        addActor(image);
        image.setOrigin(Align.center);
        image.setScale(Math.max(Constant.GAMEWIDTH/image.getWidth(),Constant.GAMEHIGHT/image.getHeight()));
        image.setPosition(Constant.GAMEWIDTH/2f,Constant.GAMEHIGHT/2f,Align.center);
        image.addAction(Actions.sequence(Actions.delay(2,Actions.alpha(0.6f,0.2f))));

        stage.addListener(new ClickListener(){

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                image.clearActions();
                image.addAction(Actions.alpha(1.0f,0.2f));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                image.clearActions();
                image.addAction(Actions.delay(1,Actions.run(()->{
                    image.addAction(Actions.alpha(0.7f,0.2f));
                })));
            }
        });
    }
}
