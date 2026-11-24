package com.joker.puzzle.wallpaper;
import kw.manager.core.*;
import kw.manager.core.city.*;
public final class CityWallpaperService extends BaseWallpaperService {
    @Override protected WallpaperGame<?> createGame(WallpaperPlatform platform) { return new WeatherCityGame(platform, new WeatherSettings(), true); }
}
