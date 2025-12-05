package kw.manager.core.hhbb;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Group;

public class ShaderBaseGroup extends Group {
    protected ShaderProgram program ;
    public ShaderBaseGroup(String vert,String frag){
        program = new ShaderProgram(
                Gdx.files.internal(vert),
                Gdx.files.internal(frag));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.flush();
        batch.setShader(program);
        setPar();
        super.draw(batch, parentAlpha);
        batch.flush();
        batch.setShader(null);
    }

    public void setPar(){}
}
