package kw.manager.core.group;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;

import kw.manager.core.asset.ManagerAsset;

public class TileGroup extends Group {
    private Image selectBottom;
    public TileGroup(String btn){
        setSize(400,200);
        Image btnFont = new Image(ManagerAsset.getAsset().getTexture(btn));
        addActor(btnFont);
        btnFont.setPosition(getWidth()/2f,getHeight()/2f, Align.center);

        selectBottom = new Image(new NinePatch(
                ManagerAsset.getAsset().getTexture("viewTexture/itemx.png"),20,20,1,1));
        addActor(selectBottom);
        selectBottom.setWidth(getWidth()-30);
        selectBottom.setPosition(getWidth()/2f,20,Align.bottom);
        selectBottom.setOrigin(Align.center);
        selectBottom.setScale(0);

        selectBottom.getColor().a = 0;
    }

    public void show() {
        selectBottom.addAction(
                Actions.parallel(
                        Actions.scaleTo(1,1,0.2f),
                        Actions.fadeIn(0.2f)
                )
        );
    }

    public void hide() {

        selectBottom.addAction(
                Actions.parallel(
                        Actions.scaleTo(0,0,0.2f),
                        Actions.fadeOut(0.2f)
                )
        );
    }
}
