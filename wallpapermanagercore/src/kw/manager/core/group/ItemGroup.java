package kw.manager.core.group;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.wallper.asset.Asset;
import com.wallper.pre.GamePre;

import kw.manager.core.ManagerConstant;

public class ItemGroup extends Group {
    private String nameString;
    public ItemGroup(Texture texture,String nameLable){
        this.nameString = nameLable;
        Image bg = new Image(Asset.getAsset().getTexture("ui/viewTexture/itembg.png"));
        addActor(bg);
        setSize(bg.getWidth(),bg.getHeight());
        Image imagebg = new Image(texture);
        addActor(imagebg);
        imagebg.setPosition(getWidth()/2f,getHeight()/2f, Align.center);
        imagebg.setOrigin(Align.center);
        imagebg.setScale(Math.min(getWidth()/imagebg.getWidth(),getHeight()/imagebg.getHeight()));
        Image bottomShadow = new Image(new NinePatch(
                Asset.getAsset().getTexture("ui/viewTexture/iteminfo.png"),20,20,20,20));
        addActor(bottomShadow);
        bottomShadow.setWidth(getWidth());
        bottomShadow.setPosition(getWidth()/2f,0,Align.bottom);


        Label nameLabel = new Label(nameLable,new Label.LabelStyle(){
            {
                font = Asset.getAsset().loadBitFont("front/Manrope-Bold_38.fnt");
            }
        });
        addActor(nameLabel);
        nameLabel.setAlignment(Align.left);
        nameLabel.setPosition(bottomShadow.getWidth()/2f , bottomShadow.getHeight()/2f,Align.center);


        addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if (openType == 1){
                    GamePre.getInstance().saveWall(nameString);
                }else {
                    GamePre.getInstance().savePet(nameString);
                }
                ManagerConstant.windowListener.setWallOrPet(openType);
            }
        });
    }

    private int openType;
    public void setWallOrPet(int i) {
        this.openType = i;
    }
}
