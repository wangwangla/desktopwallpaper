package kw.manager.core.hhbb;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.wallper.asset.Asset;
import com.wallper.constant.Constant;

import kw.manager.core.event.EventListener;
import kw.manager.core.event.EventRun;


public class HmBaoBao extends ShaderBaseGroup {
    private float touch[] = new float[2];
    private float time;
    Image image;
    private Rectangle rectangle;
    private float delayTime ;
    private float distanceTime;

    private float waveStrength = 0f;
    private float waveTime = 0f;
    private float waveDuration = 4.0f;  // 波纹持续时间（秒）
    private float wave_radius;


    public HmBaoBao(Rectangle rectangle) {
        super("shader/wave/wave.vert","shader/wave/wave.glsl");

        image = new Image(Asset.getAsset().getTexture("wallResource/wallpaper/SpongeBob SquarePants/SpongeBob SquarePants.png"));
        this.rectangle = rectangle;
        addActor(image);
        image.setPosition(0,0, Align.center);
        this.rectangle.set(0,0,image.getWidth(),image.getHeight());
        addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                touch[0] = (x / image.getWidth()) + 0.5f;
                touch[1] = 1.0f - (y / image.getHeight()) - 0.5f;
                waveStrength = 1f;
                waveTime = 0f;
                wave_radius = 0;
                lj = 0;
                time = 0;
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        EventListener.getInstance().addEvent("mousePosisiton", new EventRun() {
            @Override
            public void run(float x, float y) {

                if (getStage()!=null) {
                    Vector2 stagePos = getStage().screenToStageCoordinates(new Vector2(x, y));
                    HmBaoBao.this.stageToLocalCoordinates(stagePos);
                    touch[0] = (stagePos.x / image.getWidth()) + 0.5f;
                    touch[1] = 1.0f - (stagePos.y / image.getHeight()) - 0.5f;
                    // 触发新波纹
                    wave_radius = 0;
                    lj = 0;
                    time = 0;

                    waveStrength = 1f;
                    waveTime = 0f;
                }

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
        program.setUniformf("wave_offset",wave_radius);
        program.setUniform2fv("center",touch,0,2);

        program.setUniformf("wave_radius",0.3f);
    }

    private float lj = 0;

    @Override
    public void act(float delta) {
        super.act(delta);
        lj += delta;
        time =lj * 1f;

        wave_radius = lj;
        if (wave_radius>3f)return;

//        delayTime += delta;
//        if (delayTime>distanceTime){
//            distanceTime = (float) (4+Math.random() * 4);
//            float xx = (float) (Math.random() * rectangle.getWidth());
//            float yy = (float) (Math.random() * rectangle.getHeight());
//            touch[0] = (xx / image.getWidth()) + 0.5f;
//            touch[1] = 1.0f - (yy / image.getHeight()) - 0.5f;
//            delayTime = 0;
//        }
    }
}