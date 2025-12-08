package kw.manager.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.wallper.asset.Asset;
import com.wallper.constant.Constant;
import com.wallper.pre.GamePre;
import com.wallper.screen.BasePetGame;
import com.wallper.screen.BasePetScreen;

import kw.manager.core.group.EightDiagramGroup;
import kw.manager.core.hhbb.HmBaoBao;
import kw.manager.core.hhbb.WaterGroup;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/5 10:48
 */
public class MainScreen extends BasePetScreen {

    public MainScreen(BasePetGame game) {
        super(game);
    }

    @Override
    public void initView() {
        super.initView();
        Image image = new Image(Asset.getAsset().getTexture("ui/white.png"));
        addActor(image);
        image.setColor(Color.valueOf("#f0d9ac"));
        image.setSize(Constant.GAMEWIDTH,Constant.GAMEHIGHT);
        image.setPosition(Constant.GAMEWIDTH/2.0f,Constant.GAMEHIGHT/2.0f, Align.center);

        float offsetX1 = offsetX;
        float offsetY1 = offsetY;


        stage.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                System.out.println("xxxxxxxxxxxxxxxxxxxxxxxx");
            }
        });

        String wall = GamePre.getInstance().getWall();
        System.out.println(wall);
        if ("".equals(wall)){
            wall = "pendulum";
        }

//        if (wall.equals("SpongeBob SquarePants")){
//            HmBaoBao hmBaoBao = new HmBaoBao(new Rectangle(-offsetX1,-offsetY1,Constant.GAMEWIDTH,Constant.GAMEWIDTH));
//            addActor(hmBaoBao);
//            hmBaoBao.setPosition(Constant.GAMEWIDTH/2f,Constant.GAMEHIGHT/2f,Align.center);
//        }else if (wall.equals("Sixty-four hexagrams")){
//            EightDiagramGroup group = new EightDiagramGroup();
//            addActor(group);
//            group.showImg();
//            group.showGroup();
//            group.setPosition(Constant.GAMEWIDTH/2.0f,Constant.GAMEHIGHT/2.0f,Align.center);
//        }else if (wall.equals("pendulum")){
//            Image image1 = new Image(Asset.getAsset().getTexture("baichui.png"));
//            addActor(image1);
//            image1.setPosition(image.getX(Align.center),image.getY(Align.center),Align.center);
//            image1.setOrigin(Align.top);
//            image1.addAction(Actions.forever(
//                    Actions.sequence(
//                            Actions.rotateTo(-40,   1, Interpolation.fastSlow),
//                            Actions.rotateTo(0,     1, Interpolation.slowFast),
//                            Actions.rotateTo(40,1,Interpolation.fastSlow),
//                            Actions.rotateTo(0, 1,Interpolation.slowFast)
//                    )
//            ));
//        }

        HmBaoBao hmBaoBao = new HmBaoBao(new Rectangle(-offsetX1,-offsetY1,Constant.GAMEWIDTH,Constant.GAMEWIDTH));
        addActor(hmBaoBao);
        hmBaoBao.setPosition(Constant.GAMEWIDTH/2f,Constant.GAMEHIGHT/2f,Align.center);

//        SpineActor spineActor = new SpineActor("3_75_32");
//        addActor(spineActor);
//        spineActor.setAnimation("animation",true);
//        spineActor.setPosition(Constant.GAMEWIDTH/2,Constant.GAMEHIGHT/2.0f,Align.center);



    }
}
