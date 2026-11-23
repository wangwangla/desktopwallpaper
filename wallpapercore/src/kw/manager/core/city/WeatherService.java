package kw.manager.core.city;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

public final class WeatherService implements AutoCloseable {
    private final Supplier<WeatherSettings> settings;
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "weather-city-network"); thread.setDaemon(true); return thread;
    });
    public volatile WeatherData data = new WeatherData();
    public volatile String status = "正在获取天气…";

    public WeatherService(Supplier<WeatherSettings> settings, boolean online) {
        this.settings = settings;
        WeatherSettings initial = settings.get(); data.latitude = initial.latitude; data.longitude = initial.longitude;
        try {
            WeatherData cached = WeatherSettings.json().fromJson(WeatherData.class,
                new String(Files.readAllBytes(cache().toPath()), StandardCharsets.UTF_8));
            if (samePlace(cached, initial)) { data = cached; status = "显示最近的天气数据"; }
        } catch (Exception ignored) {}
        if (online) worker.scheduleWithFixedDelay(() -> {
            if (data.fetchedAt == 0 || System.currentTimeMillis()-data.fetchedAt >= TimeUnit.MINUTES.toMillis(15)) refresh();
        }, 0, 1, TimeUnit.MINUTES);
        else status = "场景预览";
    }

    private File cache() { return new File(WeatherSettings.directory(), "weather-cache.json"); }
    private static boolean samePlace(WeatherData data, WeatherSettings config) {
        return Math.abs(data.latitude - config.latitude) < 0.0001 && Math.abs(data.longitude - config.longitude) < 0.0001;
    }
    public void refreshSoon() { worker.execute(this::refresh); }
    public void changingLocation(WeatherSettings config) {
        WeatherData pending = new WeatherData();
        pending.latitude = config.latitude; pending.longitude = config.longitude; pending.timezone = config.timezone;
        data = pending; status = "正在获取新地点的天气…";
        refreshSoon();
    }

    private void refresh() {
        WeatherSettings config = settings.get().copy();
        try {
            String address = "https://api.open-meteo.com/v1/forecast?latitude=" + config.latitude + "&longitude="
                + config.longitude + "&current=temperature_2m,cloud_cover,precipitation,weather_code,wind_speed_10m"
                + "&daily=sunrise,sunset&forecast_days=2&timezone=" + URLEncoder.encode(config.timezone, "UTF-8");
            JsonValue root = get(address), current = root.get("current"), daily = root.get("daily");
            WeatherData result = new WeatherData();
            result.latitude = config.latitude; result.longitude = config.longitude;
            result.timezone = root.getString("timezone", config.timezone);
            result.temperature = current.getFloat("temperature_2m", 22);
            result.cloudCover = Math.max(0, Math.min(100, current.getFloat("cloud_cover", 35)));
            result.precipitation = Math.max(0, current.getFloat("precipitation", 0));
            result.weatherCode = current.getInt("weather_code", 2);
            result.windSpeed = Math.max(0, current.getFloat("wind_speed_10m", 8));
            result.fetchedAt = System.currentTimeMillis();
            result.dates = daily.get("time").asStringArray();
            result.sunrise = daily.get("sunrise").asStringArray(); result.sunset = daily.get("sunset").asStringArray();
            if (samePlace(result, settings.get())) {
                data = result; status = "实时天气";
                Files.write(cache().toPath(), WeatherSettings.json().prettyPrint(result).getBytes(StandardCharsets.UTF_8));
                System.out.println("Weather updated from Open-Meteo.");
            }
        } catch (Exception failure) {
            System.err.println("Weather update failed: "+failure.getClass().getSimpleName()+": "+failure.getMessage());
            status = data.fetchedAt > 0 ? "天气暂时无法更新，继续显示最近数据" : "暂时无法获取天气，显示默认场景";
        }
    }

    public static final class Place {
        public String name, country, region, timezone;
        public double latitude, longitude;
        @Override public String toString() { return name + " · " + (region == null ? "" : region + " · ") + country; }
    }
    public static List<Place> search(String city) throws IOException {
        Map<String, String> aliases = new HashMap<>();
        aliases.put("香港", "Hong Kong"); aliases.put("深圳", "Shenzhen"); aliases.put("广州", "Guangzhou");
        aliases.put("北京", "Beijing"); aliases.put("上海", "Shanghai"); aliases.put("成都", "Chengdu");
        aliases.put("杭州", "Hangzhou"); aliases.put("武汉", "Wuhan"); aliases.put("南京", "Nanjing");
        aliases.put("重庆", "Chongqing"); aliases.put("台北", "Taipei"); aliases.put("澳门", "Macau");
        String query = aliases.getOrDefault(city.trim(), city.trim());
        JsonValue root = get("https://geocoding-api.open-meteo.com/v1/search?count=5&language=zh&format=json&name="
            + URLEncoder.encode(query, "UTF-8"));
        List<Place> places = new ArrayList<>();
        JsonValue results = root.get("results");
        if (results != null) for (JsonValue row : results) {
            Place place = new Place(); place.name = row.getString("name"); place.country = row.getString("country", "");
            place.region = row.getString("admin1", ""); place.timezone = row.getString("timezone", "UTC");
            place.latitude = row.getDouble("latitude"); place.longitude = row.getDouble("longitude"); places.add(place);
        }
        return places;
    }

    static JsonValue get(String address) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(8000); connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent", "DesktopWallpaper-WeatherCity/1.0");
        try {
            if (connection.getResponseCode() != 200) throw new IOException("Weather service returned " + connection.getResponseCode());
            try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                StringBuilder text = new StringBuilder(); char[] buffer = new char[4096]; int count;
                while ((count = reader.read(buffer)) >= 0) {
                    text.append(buffer, 0, count); if (text.length() > 2_000_000) throw new IOException("Response too large");
                }
                return new JsonReader().parse(text.toString());
            }
        } finally { connection.disconnect(); }
    }
    @Override public void close() { worker.shutdownNow(); }
}
