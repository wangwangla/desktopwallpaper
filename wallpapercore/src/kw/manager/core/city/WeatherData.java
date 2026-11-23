package kw.manager.core.city;

public class WeatherData {
    public double latitude, longitude;
    public float temperature = 22f, cloudCover = 35f, precipitation = 0f, windSpeed = 8f;
    public int weatherCode = 2;
    public long fetchedAt;
    public String timezone = "Asia/Hong_Kong";
    public String[] dates = new String[0], sunrise = new String[0], sunset = new String[0];

    public WeatherData copy() {
        WeatherData copy = new WeatherData();
        copy.latitude = latitude; copy.longitude = longitude; copy.temperature = temperature;
        copy.cloudCover = cloudCover; copy.precipitation = precipitation; copy.windSpeed = windSpeed;
        copy.weatherCode = weatherCode; copy.fetchedAt = fetchedAt; copy.timezone = timezone;
        copy.dates = dates; copy.sunrise = sunrise; copy.sunset = sunset;
        return copy;
    }

    public WeatherData preview(String mode) {
        WeatherData value = copy();
        if ("clear".equals(mode)) { value.weatherCode = 0; value.cloudCover = 5; value.precipitation = 0; }
        if ("cloudy".equals(mode)) { value.weatherCode = 2; value.cloudCover = 60; value.precipitation = 0; }
        if ("overcast".equals(mode)) { value.weatherCode = 3; value.cloudCover = 100; value.precipitation = 0; }
        if ("rain".equals(mode)) { value.weatherCode = 63; value.cloudCover = 100; value.precipitation = 2.5f; }
        if ("fog".equals(mode)) { value.weatherCode = 45; value.cloudCover = 85; value.precipitation = 0; }
        if ("snow".equals(mode)) { value.weatherCode = 73; value.cloudCover = 95; value.precipitation = 1; }
        return value;
    }

    public boolean snow() { return weatherCode >= 71 && weatherCode <= 77 || weatherCode == 85 || weatherCode == 86; }
    public boolean fog() { return weatherCode == 45 || weatherCode == 48; }
    public String description() {
        if (weatherCode == 0) return "晴";
        if (weatherCode == 1) return "大部晴朗";
        if (weatherCode == 2) return "多云";
        if (weatherCode == 3) return "阴天";
        if (fog()) return "雾";
        if (snow()) return "雪";
        if (weatherCode >= 95) return "雷雨";
        if (weatherCode >= 51) return "雨";
        return "多云";
    }
}
