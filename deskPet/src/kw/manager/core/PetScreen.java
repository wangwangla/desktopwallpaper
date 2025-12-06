package kw.manager.core;

import com.badlogic.gdx.scenes.scene2d.ui.Image;

import kw.manager.core.asset.Asset;
import kw.manager.core.base.BaseGame;
import kw.manager.core.base.BaseScreen;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/5 10:48
 */
public class PetScreen extends BaseScreen {
    public PetScreen(BaseGame game) {
        super(game);
    }

    @Override
    public void initView() {
        super.initView();
        Image image = new Image(Asset.getAsset().getTexture("resource/zhuochong/img.png"));
//        addActor(image);


    }
}
