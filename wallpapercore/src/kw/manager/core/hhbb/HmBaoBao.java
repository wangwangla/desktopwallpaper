package kw.manager.core.hhbb;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.wallper.asset.Asset;
import com.wallper.constant.Constant;


public class HmBaoBao extends ShaderBaseGroup {
    private float touch[] = new float[2];
    private float time;
    Image image;
    private Rectangle rectangle;
    private float delayTime ;
    private float distanceTime;
    public HmBaoBao(Rectangle rectangle) {
        super("shader/wave/wave.vert","shader/wave/wave.glsl");
        image = new Image(Asset.getAsset().getTexture("hmbb.jpg"));
        this.rectangle = rectangle;
        addActor(image);
        image.setPosition(0,0, Align.center);
        this.rectangle.set(0,0,image.getWidth(),image.getHeight());
        addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                touch[0] = (x / image.getWidth()) + 0.5f;
                touch[1] = 1.0f - (y / image.getHeight()) - 0.5f;
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        float max = Math.max(Constant.GAMEWIDTH /image.getWidth(), Constant.GAMEHIGHT / image.getHeight());
        setScale(Math.min(max,1.0f));

        distanceTime = (float) (4+Math.random() * 4);
    }

    @Override
    public void setPar() {
        super.setPar();
        program.setUniformf("time",time);
        program.setUniformf("wave_offset",0.2f);
        program.setUniform2fv("center",touch,0,2);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        time += delta * 10;

        delayTime += delta;
        if (delayTime>distanceTime){
            distanceTime = (float) (4+Math.random() * 4);
            float xx = (float) (Math.random() * rectangle.getWidth());
            float yy = (float) (Math.random() * rectangle.getHeight());
            touch[0] = (xx / image.getWidth()) + 0.5f;
            touch[1] = 1.0f - (yy / image.getHeight()) - 0.5f;
            delayTime = 0;
        }
    }
}