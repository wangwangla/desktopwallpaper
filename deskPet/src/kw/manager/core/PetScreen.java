package kw.manager.core;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
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
    }
}
