package kw.manager.core.demo;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.kw.gdx.BaseGame;
import com.kw.gdx.asset.Asset;
import com.kw.gdx.constant.Constant;
import com.kw.gdx.screen.BaseScreen;

public class DemoScreen extends BaseScreen {
    private Image leftImg;
    private Image right;
    private Image top;
    private Image bottom;
    public DemoScreen(BaseGame game) {
        super(game);
    }

    @Override
    public void initView() {
        super.initView();
        leftImg = new Image(Asset.getAsset().getTexture("assets/gongdeNum.png"));
        right = new Image(Asset.getAsset().getTexture("assets/gongdeNum.png"));
        top = new Image(Asset.getAsset().getTexture("assets/gongdeNum.png"));
        bottom = new Image(Asset.getAsset().getTexture("assets/gongdeNum.png"));
        addActor(leftImg);
        addActor(right);
        addActor(top);
        addActor(bottom);
        layerUI();
    }

    public void layerUI(){
        leftImg.setPosition(0,Constant.GAMEHIGHT/2, Align.center);
        right.setPosition(Constant.GAMEWIDTH,Constant.GAMEHIGHT/2, Align.center);
        top.setPosition(Constant.GAMEWIDTH/2,Constant.GAMEHIGHT, Align.center);
        bottom.setPosition(Constant.GAMEWIDTH/2,0, Align.center);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        System.out.println("resize===============");
        layerUI();
    }
}
