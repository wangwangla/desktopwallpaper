package kw.manager.core;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.wallper.asset.Asset;
import com.wallper.constant.Constant;
import com.wallper.pre.GamePre;
import com.wallper.screen.BasePetGame;
import com.wallper.screen.BasePetScreen;

import kw.manager.core.csv.CsvReadFile1;
import kw.manager.core.csv.DataBean;

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

        CsvReadFile1.readCw();
        ArrayMap<String,DataBean> chongwuMapAll = CsvReadFile1.getChongwuMapAll();


        String pet = GamePre.getInstance().getPet();
        String path = "wallResource/zhuochong/dog/dog.png";
        if ("".equals(pet)){

        }else {
            DataBean dataBean = chongwuMapAll.get(pet);
            path  = "wallResource/zhuochong/"+dataBean.getName()+"/"+dataBean.getResourceName()+".png";

        }

        Image image = new Image(Asset.getAsset().getTexture(path));
        addActor(image);
        image.setOrigin(Align.center);
        image.setScale(Math.min(Constant.GAMEWIDTH/image.getWidth(),Constant.GAMEHIGHT/image.getHeight()));
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
                image.addAction(Actions.delay(1,Actions.run(()->{
                    image.clearActions();
                    image.addAction(Actions.alpha(0.7f,0.2f));
                })));
            }
        });
    }
}
