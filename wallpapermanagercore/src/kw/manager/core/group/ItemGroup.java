package kw.manager.core.group;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

import kw.manager.core.asset.ManagerAsset;

public class ItemGroup extends Group {
    public ItemGroup(Texture texture,String name){

        Image bg = new Image(ManagerAsset.getAsset().getTexture("viewTexture/itembg.png"));
        addActor(bg);
        setSize(bg.getWidth(),bg.getHeight());
        Image imagebg = new Image(texture);
        addActor(imagebg);
        imagebg.setPosition(getWidth()/2f,getHeight()/2f, Align.center);
        imagebg.setOrigin(Align.center);
        imagebg.setScale(Math.min(getWidth()/imagebg.getWidth(),getHeight()/imagebg.getHeight()));
        Image bottomShadow = new Image(new NinePatch(
                ManagerAsset.getAsset().getTexture("viewTexture/iteminfo.png"),20,20,20,20));
        addActor(bottomShadow);
        bottomShadow.setWidth(getWidth());
        bottomShadow.setPosition(getWidth()/2f,0,Align.bottom);


        Label nameLabel = new Label(name,new Label.LabelStyle(){
            {
                font = ManagerAsset.getAsset().loadBitFont("front/Manrope-Bold_38.fnt");
            }
        });
        addActor(nameLabel);
        nameLabel.setAlignment(Align.left);
        nameLabel.setPosition(bottomShadow.getWidth()/2f , bottomShadow.getHeight()/2f,Align.center);
    }
}
