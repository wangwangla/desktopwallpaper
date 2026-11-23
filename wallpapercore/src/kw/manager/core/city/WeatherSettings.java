package kw.manager.core.city;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.ZoneId;

/** Settings are kept outside the assets and outside the repository. */
public class WeatherSettings {
    public String city = "香港";
    public double latitude = 22.3193;
    public double longitude = 114.1694;
    public String timezone = "Asia/Hong_Kong";
    public String weatherMode = "real";
    public int previewMinutes = -1;
    public String modelPath = "";

    public WeatherSettings copy() {
        WeatherSettings result = new WeatherSettings();
        result.city = city; result.latitude = latitude; result.longitude = longitude;
        result.timezone = timezone; result.weatherMode = weatherMode;
        result.previewMinutes = previewMinutes; result.modelPath = modelPath;
        return result;
    }

    public static File directory() {
        String root = System.getenv("LOCALAPPDATA");
        if (root == null || root.isEmpty()) root = System.getProperty("user.home");
        File directory = new File(root, "DesktopWallpaper/weather-city");
        directory.mkdirs();
        return directory;
    }

    public static Json json() {
        Json json = new Json();
        json.setIgnoreUnknownFields(true);
        json.setUsePrototypes(false);
        json.setOutputType(JsonWriter.OutputType.json);
        return json;
    }

    public static WeatherSettings load() {
        File file = new File(directory(), "settings.json");
        try {
            WeatherSettings settings = json().fromJson(WeatherSettings.class,
                new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
            if (!Double.isFinite(settings.latitude) || Math.abs(settings.latitude) > 90
                || !Double.isFinite(settings.longitude) || Math.abs(settings.longitude) > 180) return new WeatherSettings();
            ZoneId.of(settings.timezone);
            settings.previewMinutes = Math.max(-1, Math.min(1439, settings.previewMinutes));
            return settings;
        } catch (Exception ignored) { return new WeatherSettings(); }
    }

    public void save() {
        try {
            Files.write(new File(directory(), "settings.json").toPath(),
                json().prettyPrint(this).getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) { /* The current session still works without persistence. */ }
    }
}
