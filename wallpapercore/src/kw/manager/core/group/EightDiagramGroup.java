package kw.manager.core.group;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.wallper.asset.Asset;

import kw.manager.core.data.EightDiagramData;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/10 21:08
 */
public class EightDiagramGroup extends Group {
    private Array<String> diagramDataArray;
    private int index = 0;
    private Array<Group> targetGroup;
    public EightDiagramGroup(){
        EightDiagramData diagramData = new EightDiagramData();
        this.diagramDataArray = diagramData.eightData();
        targetGroup = new Array<>();
        setSize(312,292);
    }

    public void showImg(){
        for (String str : diagramDataArray) {
            Group group = new Group();
            group.setSize(213,292);
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (c == '0'){
                    Image img = new Image(Asset.getAsset().getTexture("wallResource/wallpaper/Sixty-four hexagrams/0.png"));
                    group.addActor(img);
                    img.setX(group.getWidth()/2.0f, Align.center);
                    img.setY(group.getHeight() - i * 24 - i * 31,Align.top);
                }else {
                    Image img = new Image(Asset.getAsset().getTexture("wallResource/wallpaper/Sixty-four hexagrams/1.png"));
                    group.addActor(img);
                    img.setX(group.getWidth()/2.0f, Align.center);
                    img.setY(group.getHeight() - i * 24 - i * 31,Align.top);
                }
            }
            targetGroup.add(group);
        }
    }

    public void showGroup(){
        Group oldGroup = targetGroup.get(index);
        index = (index+1)%64;
        Group current = targetGroup.get(index);
//        stage.addActor(oldGroup);
        addActor(current);
        current.getColor().a = 0;
        current.addAction(Actions.fadeIn(0.1f));
        oldGroup.addAction(Actions.sequence(
                Actions.fadeOut(0.1f),
                Actions.removeActor()));
        addAction(Actions.delay(0.5f,Actions.run(()->{
            showGroup();
        })));
    }
}
