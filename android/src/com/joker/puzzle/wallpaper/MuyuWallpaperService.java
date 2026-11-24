package com.joker.puzzle.wallpaper;
import kw.manager.core.*;
import kw.manager.core.muyu.MuyuGame;
public final class MuyuWallpaperService extends BaseWallpaperService {
    @Override protected WallpaperGame<?> createGame(WallpaperPlatform platform) { return new MuyuGame(platform, true, null); }
}
