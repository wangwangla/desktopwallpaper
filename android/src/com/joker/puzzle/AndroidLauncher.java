package com.joker.puzzle;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import com.joker.puzzle.wallpaper.*;

/** Android only handles the system picker; rendering lives in wallpapercore. */
public class AndroidLauncher extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int)(24 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);
        TextView title = new TextView(this);
        title.setText("动态壁纸"); title.setTextSize(28); layout.addView(title);
        TextView help = new TextView(this);
        help.setText("选择壁纸，在系统预览中点击设置壁纸。设置后由 Android 自动管理启动和暂停。天气默认使用香港。");
        layout.addView(help);
        addWallpaper(layout, "木鱼", MuyuWallpaperService.class);
        addWallpaper(layout, "二十四节气 · 实时天气", SeasonsWallpaperService.class);
        addWallpaper(layout, "天气城市", CityWallpaperService.class);
        setContentView(layout);
    }
    private void addWallpaper(LinearLayout parent, String label, Class<?> service) {
        Button button = new Button(this); button.setText(label);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(this, service));
            try { startActivity(intent); }
            catch (android.content.ActivityNotFoundException e) {
                try { startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)); }
                catch (android.content.ActivityNotFoundException unavailable) {
                    Toast.makeText(this, "此设备不支持动态壁纸", Toast.LENGTH_LONG).show();
                }
            }
        }); parent.addView(button);
    }
}
