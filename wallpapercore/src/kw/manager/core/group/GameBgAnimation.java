package kw.manager.core.group;



import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.wallper.asset.Asset;
import com.wallper.constant.Constant;


public class GameBgAnimation extends Group {
    private final ShaderProgram program;
    private float time = 4f;
    private final float flash = 0f;
    public GameBgAnimation(){
        ShaderProgram shaderProgram = new ShaderProgram(
                Gdx.files.internal("shader/xiaochoupai/b.vert"),
                Gdx.files.internal("shader/xiaochoupai/b.frag")
        );
        if (!shaderProgram.isCompiled()) {
            Gdx.app.error("GameBgAnimation", "Shader compile failed, fallback to normal image render.\n" + shaderProgram.getLog());
            shaderProgram.dispose();
            shaderProgram = null;
        }
        program = shaderProgram;
        setSize(Constant.GAMEWIDTH,Constant.GAMEHIGHT);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (program == null) {
            super.draw(batch, parentAlpha);
            return;
        }
        batch.setShader(program);
        program.setUniformf("time", time * 0.7f);
        program.setUniformf("vort_speed", 4.0f);
        program.setUniformf("mid_flash", flash);
        program.setUniformf("vort_offset", 0.0f);
        program.setUniformf("colour_1", new Color(0.4f, 0.7f, 1f, 1f));
        program.setUniformf("colour_2", new Color(0.8f, 0.2f, 1f, 1f));
        super.draw(batch, parentAlpha);
        batch.setShader(null);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        time += delta;


    }
}