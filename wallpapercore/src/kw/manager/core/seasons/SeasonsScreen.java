package kw.manager.core.seasons;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.*;
import com.kw.gdx.screen.BaseScreen;
import kw.manager.core.WallpaperAssets;
import kw.manager.core.city.*;

public final class SeasonsScreen extends BaseScreen {
    private volatile SeasonSettings settings;
    public final WeatherService weather;
    private final WallpaperAssets assets=new WallpaperAssets();
    private final SeasonalScene scene;
    private final SeasonsGame seasonsGame;
    public SeasonsScreen(SeasonsGame game){super(game);seasonsGame=game;settings=game.initialSettings.copy();weather=new WeatherService(()->settings,game.interactive);scene=new SeasonalScene(assets,this);}
    public SeasonSettings settings(){return settings.copy();}
    public void configure(SeasonSettings next){SeasonSettings before=settings;settings=next.copy();settings.save();if(before.latitude!=next.latitude||before.longitude!=next.longitude||!before.timezone.equals(next.timezone))weather.changingLocation(settings);}
    public void preview(int term,int minutes,String mode,float temperature){SeasonSettings s=settings();s.previewTerm=term;s.previewMinutes=minutes;s.weatherMode=mode;settings=s;WeatherData data=new WeatherData();data.temperature=temperature;weather.data=data;scene.resetTime();}
    @Override public void initView(){if(scene.getStage()==null)stage.addActor(scene);stage.addListener(new InputListener(){@Override public boolean keyDown(InputEvent event,int key){
        if(!seasonsGame.interactive)return false;
        if(key==Input.Keys.ESCAPE){if(seasonsGame.platform().isWallpaper())seasonsGame.platform().preview();else seasonsGame.platform().quit();return true;}
        if(key==Input.Keys.F1){seasonsGame.platform().showControls();return true;}return false;
    }});}
    @Override public void dispose(){weather.close();super.dispose();assets.close();}
}
