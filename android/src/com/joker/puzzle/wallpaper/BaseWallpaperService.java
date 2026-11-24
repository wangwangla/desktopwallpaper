package com.joker.puzzle.wallpaper;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService;
import kw.manager.core.*;

public abstract class BaseWallpaperService extends AndroidLiveWallpaperService {
    protected abstract WallpaperGame<?> createGame(WallpaperPlatform platform);
    @Override public void onCreateApplication() {
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useGyroscope = false;
        config.useWakelock = false;
        config.getTouchEventsForLiveWallpaper = true;
        config.r = config.g = config.b = 8;
        initialize(createGame(new AndroidWallpaperPlatform(this)), config);
    }
}
