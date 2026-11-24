package kw.manager.core.city;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import java.time.*;
import java.util.Random;

final class CitySky implements Disposable {
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch sprites = new SpriteBatch();
    private final kw.manager.core.WallpaperAssets assets=new kw.manager.core.WallpaperAssets();
    private final Texture sun=assets.texture("city/sun.png"),cloud=assets.texture("weather/cloud.png");
    private Texture moon;
    private int cachedPhase=-1;
    private final float[][] stars = new float[140][3], clouds = new float[22][4], drops = new float[420][4];
    final Color horizon = new Color(), top = new Color();
    float daylight, night;
    private float elapsed;

    CitySky() {
        Random random = new Random(1955);
        for (float[] point : stars) { point[0] = random.nextFloat(); point[1] = .60f + random.nextFloat() * .40f; point[2] = .65f + random.nextFloat(); }
        for (float[] shape : clouds) { shape[0] = random.nextFloat() * 1.5f; shape[1] = .60f + random.nextFloat() * .44f; shape[2] = .18f + random.nextFloat() * .28f; shape[3] = .5f + random.nextFloat(); }
        for (float[] drop : drops) { drop[0] = random.nextFloat(); drop[1] = random.nextFloat(); drop[2] = .5f + random.nextFloat(); drop[3] = random.nextFloat() * 6.28f; }
    }

    void sky(int width, int height, float delta, ZonedDateTime time, Astronomy.Sun solar, WeatherData weather) {
        elapsed += Math.min(delta, .1f);
        daylight = smooth((float) ((solar.altitude + 6) / 13)); night = 1 - daylight;
        float gray = weather.cloudCover / 100f;
        top.set(.008f,.016f,.039f,1).lerp(new Color(.20f,.41f,.63f,1),daylight);
        horizon.set(.045f,.065f,.105f,1).lerp(new Color(.66f,.74f,.79f,1),daylight);
        top.lerp(new Color(.40f,.44f,.49f,1), gray * daylight * .72f);
        horizon.lerp(new Color(.58f,.60f,.62f,1), gray * daylight * .65f);
        float dusk = (1 - Math.min(1, Math.abs((float) solar.altitude - 1) / 12)) * (1-gray*.75f);
        horizon.lerp(new Color(.83f,.40f,.20f,1), dusk * .7f);
        Matrix4 projection = new Matrix4().setToOrtho2D(0,0,width,height);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND); Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(projection); shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.rect(0,0,width,height,horizon,horizon,top,top);
        for (float[] star : stars) {
            shapes.setColor(.72f,.80f,.94f,night * (1-gray) * .55f * Math.min(1,star[2]));
            shapes.circle(star[0]*width,star[1]*height,star[2]*.7f,6);
        }
        shapes.end(); sprites.setProjectionMatrix(projection); sprites.begin();
        double daySeconds = Duration.between(solar.sunrise,solar.sunset).getSeconds();
        double dayProgress = Duration.between(solar.sunrise,time).getSeconds() / Math.max(1,daySeconds);
        if (!solar.polarNight && (solar.polarDay || dayProgress >= 0 && dayProgress <= 1)) {
            float progress = clamp((float) dayProgress,0,1);
            float size = width*.10f;
            sprites.setColor(1,.72f + daylight*.26f,.41f + daylight*.57f,daylight*(1-gray*gray*gray));
            sprites.draw(sun,width*(.12f + progress*.76f)-size/2,
                height*(.70f + .26f*(float)Math.sin(progress*Math.PI))-size/2,size,size);
        }
        double phase = Astronomy.lunarPhase(time.toInstant());
        int phaseIndex=Math.min(31,(int)(phase*32));
        if(phaseIndex!=cachedPhase){moon=assets.texture("city/moon-"+phaseIndex+".png");cachedPhase=phaseIndex;}
        if (night > .1f) {
            ZonedDateTime start = time.isBefore(solar.sunrise) ? solar.sunset.minusDays(1) : solar.sunset;
            ZonedDateTime end = time.isBefore(solar.sunrise) ? solar.sunrise : solar.sunrise.plusDays(1);
            float progress = clamp(Duration.between(start,time).getSeconds() / (float)Math.max(1,Duration.between(start,end).getSeconds()),0,1);
            float size = width*.028f;
            sprites.setColor(.83f,.88f,.99f,night*Math.max(0,1-gray*1.1f));
            sprites.draw(moon,width*(.17f+progress*.66f)-size/2,
                height*(.70f+.24f*(float)Math.sin(progress*Math.PI))-size/2,size,size);
        }
        int count = Math.round(gray * clouds.length);
        for (int i=0;i<count;i++) {
            float[] value = clouds[i]; float w = width*value[2];
            float x = ((value[0]+elapsed*(.002f+weather.windSpeed*.00008f)*value[3]) % 1.6f-.35f)*width;
            sprites.setColor(.13f+daylight*.66f,.16f+daylight*.65f,.22f+daylight*.61f,.24f+gray*.60f);
            sprites.draw(cloud,x,value[1]*height-w*.13f,w,w*.40f);
        }
        sprites.setColor(Color.WHITE); sprites.end();
    }

    void precipitation(int width,int height,WeatherData weather) {
        if (weather.precipitation <= 0 && !weather.fog()) return;
        shapes.setProjectionMatrix(new Matrix4().setToOrtho2D(0,0,width,height));
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST); Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (weather.fog()) { shapes.setColor(horizon.r,horizon.g,horizon.b,.25f); shapes.rect(0,0,width,height); }
        int count = Math.min(drops.length,Math.round(weather.precipitation*100+70));
        if (weather.precipitation>0) for (int i=0;i<count;i++) {
            float[] drop=drops[i]; float speed=weather.snow()?35+drop[2]*30:600+drop[2]*420;
            float y=(drop[1]*height-elapsed*speed) % (height+50); if(y<0)y+=height+50;
            float x=drop[0]*width+(weather.snow()?(float)Math.sin(elapsed+drop[3])*16:weather.windSpeed*y*.025f);
            x=(x+width)%width;
            if(weather.snow()){shapes.setColor(.90f,.94f,.99f,.68f);shapes.circle(x,y,drop[2]*1.5f,8);}
            else {shapes.setColor(.69f,.77f,.85f,.16f+drop[2]*.08f);shapes.rectLine(x,y,x-weather.windSpeed*.10f,y+10+drop[2]*8,.7f);}
        }
        shapes.end();
    }
    private static float smooth(float value){value=clamp(value,0,1);return value*value*(3-2*value);}
    private static float clamp(float value,float min,float max){return Math.max(min,Math.min(max,value));}
    @Override public void dispose(){assets.close();shapes.dispose();sprites.dispose();}
}
