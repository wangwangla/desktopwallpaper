package com.joker.puzzle.wallpaper;

import android.content.Context;
import android.content.Intent;
import java.io.File;
import com.joker.puzzle.AndroidLauncher;
import kw.manager.core.*;
import kw.manager.core.muyu.MuyuGame;

final class AndroidWallpaperPlatform implements WallpaperPlatform {
    private final Context context;
    AndroidWallpaperPlatform(Context context) { this.context = context; }
    @Override public File storageDirectory() { return context.getFilesDir(); }
    @Override public boolean isWallpaper() { return true; }
    @Override public void wallpaper() { showControls(); }
    @Override public void preview() { showControls(); }
    @Override public void quit() { showControls(); }
    @Override public void showControls() {
        context.startActivity(new Intent(context, AndroidLauncher.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
    @Override public void ready(WallpaperGame<?> game) {
        if (game instanceof MuyuGame) ((MuyuGame)game).setWallpaper(true);
    }
}
