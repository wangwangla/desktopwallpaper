package kw.manager.core.city;

import kw.manager.core.*;
import com.kw.gdx.resource.annotation.GameInfo;

@GameInfo(width=1280,height=720)
public final class WeatherCityGame extends WallpaperGame<WeatherCityScreen> {
    public final WeatherSettings initialSettings;
    public final boolean interactive;
    public WeatherCityGame(WallpaperPlatform platform,WeatherSettings settings,boolean interactive){super(platform);this.initialSettings=settings.copy();this.interactive=interactive;}
    @Override protected void gameInfoConfig(){super.gameInfoConfig();com.kw.gdx.constant.Constant.viewColor.set(0,0,0,1);}
    @Override protected WeatherCityScreen createScreen(){return new WeatherCityScreen(this);}
}
