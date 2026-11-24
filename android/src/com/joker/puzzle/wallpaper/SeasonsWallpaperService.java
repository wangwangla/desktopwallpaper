package com.joker.puzzle.wallpaper;
import kw.manager.core.*;
import kw.manager.core.seasons.*;
public final class SeasonsWallpaperService extends BaseWallpaperService {
    @Override protected WallpaperGame<?> createGame(WallpaperPlatform platform) { return new SeasonsGame(platform, new SeasonSettings(), true); }
}
