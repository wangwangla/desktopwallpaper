package kw.manager.core.city;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.*;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.math.Vector3;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class WeatherCityScreen extends com.kw.gdx.screen.BaseScreen {
    private volatile WeatherSettings settings;
    public final WeatherService weather;
    private final CitySceneActor cityActor;
    private final kw.manager.core.WallpaperPlatform actions;
    private boolean disposed;

    public WeatherCityScreen(WeatherCityGame game) {
        super(game);WeatherSettings settings=game.initialSettings;kw.manager.core.WallpaperPlatform actions=game.platform();boolean interactive=game.interactive;
        this.settings=settings.copy();this.actions=actions;
        weather=new WeatherService(()->this.settings,interactive);
        cityActor=new CitySceneActor(()->this.settings,weather);

    }

    public WeatherSettings settings(){return settings.copy();}
    public void configure(WeatherSettings next){
        WeatherSettings before=settings;settings=next.copy();settings.save();
        if(Math.abs(before.latitude-next.latitude)>.0001||Math.abs(before.longitude-next.longitude)>.0001
            ||!before.timezone.equals(next.timezone))weather.changingLocation(settings);
    }
    public void preview(int minutes,String mode){WeatherSettings next=settings.copy();next.previewMinutes=minutes;
        next.weatherMode=mode;settings=next;}
    @Override public void initView(){
        if(cityActor.getStage()==null)stage.addActor(cityActor);
        stage.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener(){@Override public boolean keyDown(com.badlogic.gdx.scenes.scene2d.InputEvent event,int key){
            if(key==Input.Keys.F1){showController();return true;}return false;
        }});
    }
    public void showController(){actions.showControls();}
    public String clockSummary(){return cityActor.clockSummary;}
    public String sunSummary(){return cityActor.sunSummary;}
    public String weatherSummary(){WeatherData data=weather.data.preview(settings.weatherMode);
        if("real".equals(settings.weatherMode)&&data.fetchedAt==0)return settings.city+" · 等待天气更新";
        return settings.city+" · "+data.description()+" · "+Math.round(data.temperature)+"°C · 风速 "+Math.round(data.windSpeed)+" km/h";}

    @Override public void dispose(){if(disposed)return;disposed=true;weather.close();
        cityActor.dispose();super.dispose();}
}
