package kw.manager.core.group;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;

import java.util.Random;

import kw.manager.core.Constant;
import kw.manager.core.asset.Asset;

public class PicGroup extends Group {
    private Random random;
    public PicGroup(){
        random = new Random();
    }

    public void addPic(){
        FileHandle absolute = Gdx.files.internal("pic");
        FileHandle[] list = absolute.list();
        for (int i = 0; i < list.length; i++) {
            int index = random.nextInt(list.length - 1);
            FileHandle fileHandle = list[index];
            Image img = new Image(Asset.getAsset().getTexture("pic/"+fileHandle.name()));
            addActor(img);
            img.setOrigin(Align.center);
            img.setScale(0.3f);
            int i1 = random.nextInt(2) + 1;
            int i2 = random.nextInt(2) + 1;
            img.addAction(Actions.forever(Actions.sequence(
                    Actions.scaleTo(0.25f*i1,0.25f*i1,20f),
                    Actions.scaleTo(0.35f*i2,0.35f*i2,60f)

            )));
            img.setRotation(random.nextInt(180)-90);
            img.setPosition(random.nextInt((int) Constant.GAMEWIDTH-100)-180,
                    random.nextInt((int) Constant.GAMEHIGHT-100)-210);

        }
    }
}
