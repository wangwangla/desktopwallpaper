package kw.manager.core.city;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.*;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Disposable;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

/** Scene2D owns ordering and lifecycle; this Actor bridges the specialized 3D renderer. */
final class CitySceneActor extends Actor implements Disposable {
    private final Supplier<WeatherSettings> settings;
    private final WeatherService weather;
    private final CityModel city;
    private final CitySky sky = new CitySky();
    private final PerspectiveCamera camera = new PerspectiveCamera(50,1280,720);
    private final ModelBatch models = new ModelBatch(), shadows = new ModelBatch(new DepthShaderProvider());
    private final DirectionalShadowLight sunlight = new DirectionalShadowLight(2048,2048,115,115,1,220);
    private final Environment environment = new Environment();
    private final ColorAttribute ambient = new ColorAttribute(ColorAttribute.AmbientLight,new Color());
    private final ColorAttribute fog = new ColorAttribute(ColorAttribute.Fog,new Color());
    private final PointLight[] lamps = new PointLight[4];
    private final Color waterColor = new Color();

    private boolean disposed;
    volatile String clockSummary="",sunSummary="";
    private float frameDelta;
    CitySceneActor(Supplier<WeatherSettings> settings,WeatherService weather) {
        this.settings=settings;this.weather=weather;
        city=new CityModel(settings.get().modelPath);
        camera.position.set(45,26,62);camera.lookAt(0,12,0);camera.near=.5f;camera.far=450;
        environment.set(ambient);environment.set(fog);environment.add(sunlight);environment.shadowMap=sunlight;
        float[][] positions={{-19,5,1},{19,5,1},{-7,5,16},{7,5,-14}};
        for(int i=0;i<lamps.length;i++){lamps[i]=new PointLight().set(new Color(1,.65f,.33f,1),
            new Vector3(positions[i][0],positions[i][1],positions[i][2]),20);environment.add(lamps[i]);}
    }
    @Override public void act(float delta){super.act(delta);frameDelta=delta;}
    @Override public void draw(Batch batch,float parentAlpha){
        batch.end();
        try{renderScene(frameDelta);}finally{
            Gdx.gl.glViewport(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());
            Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);batch.begin();
        }
    }
    private void renderScene(float delta) {
        if(disposed)return;
        int width=Gdx.graphics.getWidth(),height=Gdx.graphics.getHeight();
        WeatherSettings config=settings.get();
        WeatherData data=weather.data.preview(config.weatherMode);
        ZonedDateTime time=Astronomy.time(config);
        Astronomy.Sun sun=Astronomy.sun(time,config.latitude,config.longitude,data);
        clockSummary=time.format(DateTimeFormatter.ofPattern("MM月dd日 HH:mm"))
            +(config.previewMinutes<0?" · 实时时间":" · 时间预览");
        sunSummary=sun.polarDay?"今天为极昼":sun.polarNight?"今天为极夜":"日出 "+sun.sunrise.format(DateTimeFormatter.ofPattern("HH:mm"))
            +"   日落 "+sun.sunset.format(DateTimeFormatter.ofPattern("HH:mm"));
        // Sky is drawn before opaque geometry. Weather particles are drawn after it.
        Gdx.gl.glViewport(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());
        sky.sky(width,height,delta,time,sun,data);
        float light=sky.daylight,cloud=data.cloudCover/100f;
        ambient.color.set(.035f+light*.29f,.052f+light*.31f,.082f+light*.33f,1);
        float power=.17f+light*(.88f-cloud*.52f);
        sunlight.color.set(power*(.65f+light*.35f),power*(.85f+light*.10f),power*(1.18f-light*.25f),1);
        double bearing=Math.toRadians(sun.azimuth),altitude=Math.toRadians(Math.max(12,sun.altitude));
        sunlight.direction.set((float)-Math.sin(bearing)*(float)Math.cos(altitude),
            -(float)Math.sin(altitude),(float)Math.cos(bearing)*(float)Math.cos(altitude)).nor();
        fog.color.set(sky.horizon);
        camera.viewportWidth=width;camera.viewportHeight=height;camera.far=data.fog()?230:450;camera.update();
        for(PointLight lamp:lamps)lamp.intensity=sky.night*80;
        waterColor.set(.019f,.05f,.082f,1).lerp(new Color(.19f,.30f,.35f,1),light);
        city.lighting(sky.night,Math.min(1,data.precipitation/2),waterColor);
        sunlight.begin(Vector3.Zero,camera.direction);
        shadows.begin(sunlight.getCamera());shadows.render(city.instance);shadows.end();sunlight.end();
        Gdx.gl.glViewport(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());
        models.begin(camera);models.render(city.instance,environment);models.end();
        sky.precipitation(width,height,data);
    }
    @Override public void dispose(){if(disposed)return;disposed=true;city.dispose();sky.dispose();models.dispose();shadows.dispose();sunlight.dispose();}
}
