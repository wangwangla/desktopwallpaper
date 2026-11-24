package kw.manager.core.seasons;

import kw.manager.core.city.WeatherSettings;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.ZoneId;

public final class SeasonSettings extends WeatherSettings {
    public int previewTerm = -1;
    public boolean showInfo = true;

    public static File folder() {
        File dir = new File(kw.manager.core.WallpaperStorage.root(), "DesktopWallpaper/seasons");
        dir.mkdirs();
        return dir;
    }

    public static SeasonSettings loadSeason() {
        try {
            SeasonSettings s = json().fromJson(SeasonSettings.class, Files.readString(new File(folder(), "settings.json").toPath(), StandardCharsets.UTF_8));
            if (s.city == null || s.weatherMode == null || !Double.isFinite(s.latitude) || Math.abs(s.latitude) > 90 || !Double.isFinite(s.longitude) || Math.abs(s.longitude) > 180)
                return new SeasonSettings();
            ZoneId.of(s.timezone);
            s.previewTerm = Math.max(-1, Math.min(23, s.previewTerm));
            s.previewMinutes = Math.max(-1, Math.min(1439, s.previewMinutes));
            return s;
        } catch (Exception e) {
            return new SeasonSettings();
        }
    }

    @Override
    public SeasonSettings copy() {
        SeasonSettings s = new SeasonSettings();
        s.city = city;
        s.latitude = latitude;
        s.longitude = longitude;
        s.timezone = timezone;
        s.weatherMode = weatherMode;
        s.previewMinutes = previewMinutes;
        s.previewTerm = previewTerm;
        s.showInfo = showInfo;
        return s;
    }

    @Override
    public void save() {
        try {
            Files.writeString(new File(folder(), "settings.json").toPath(), json().prettyPrint(this), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Unable to save seasonal settings: " + e.getMessage());
        }
    }
}
