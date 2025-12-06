package kw.manager.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import kw.manager.core.asset.Asset;
import kw.manager.core.base.BaseGame;
import kw.manager.core.base.BaseScreen;
import kw.manager.core.group.EightDiagramGroup;
import kw.manager.core.hhbb.HmBaoBao;
import kw.manager.core.spine.SpineActor;

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
        image.setColor(Color.valueOf("#f0d9ac"));
        image.setSize(Constant.GAMEWIDTH,Constant.GAMEHIGHT);
        image.setPosition(Constant.GAMEWIDTH/2.0f,Constant.GAMEHIGHT/2.0f, Align.center);

        float offsetX1 = offsetX;
        float offsetY1 = offsetY;


//        HmBaoBao hmBaoBao = new HmBaoBao(new Rectangle(-offsetX1,-offsetY1,Constant.GAMEWIDTH,Constant.GAMEWIDTH));
//        addActor(hmBaoBao);
//        hmBaoBao.setPosition(Constant.GAMEWIDTH/2f,Constant.GAMEHIGHT/2f,Align.center);





//        stage.addListener(new ClickListener(){
//            @Override
//            public void clicked(InputEvent event, float x, float y) {
//                super.clicked(event, x, y);
//            }
//        });


//        SpineActor spineActor = new SpineActor("3_75_32");
//        addActor(spineActor);
//        spineActor.setAnimation("animation",true);
//        spineActor.setPosition(Constant.GAMEWIDTH/2,Constant.GAMEHIGHT/2.0f,Align.center);

//        Image image1 = new Image(Asset.getAsset().getTexture("baichui.png"));
//        addActor(image1);
//        image1.setPosition(image.getX(Align.center),image.getY(Align.center),Align.center);
//        image1.setOrigin(Align.top);
//        image1.addAction(Actions.forever(
//                Actions.sequence(
//                    Actions.rotateTo(-40,   1, Interpolation.fastSlow),
//                    Actions.rotateTo(0,     1, Interpolation.slowFast),
//                        Actions.rotateTo(40,1,Interpolation.fastSlow),
//                        Actions.rotateTo(0, 1,Interpolation.slowFast)
//                        )
//        ));

        EightDiagramGroup group = new EightDiagramGroup();
        addActor(group);
        group.showImg();
        group.showGroup();
        group.setPosition(Constant.GAMEWIDTH/2.0f,Constant.GAMEHIGHT/2.0f,Align.center);

    }
}
