package kw.manager.core.city;

import java.time.*;

/** Solar geometry and an approximate synodic lunar phase; independent of the renderer. */
public final class Astronomy {
    private Astronomy() {}
    public static final class Sun {
        public double altitude, azimuth;
        public ZonedDateTime sunrise, sunset;
        public boolean polarDay, polarNight;
    }

    public static ZonedDateTime time(WeatherSettings settings) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(settings.timezone));
        if (settings.previewMinutes >= 0)
            now = now.withHour(settings.previewMinutes / 60).withMinute(settings.previewMinutes % 60).withSecond(0);
        return now;
    }

    public static Sun sun(ZonedDateTime time, double latitude, double longitude, WeatherData data) {
        Sun sun = new Sun();
        ZonedDateTime utc = time.withZoneSameInstant(ZoneOffset.UTC);
        double gamma = 2 * Math.PI / (utc.toLocalDate().isLeapYear() ? 366 : 365)
            * (utc.getDayOfYear() - 1 + (utc.getHour() - 12) / 24.0);
        double equation = 229.18 * (0.000075 + 0.001868 * Math.cos(gamma) - 0.032077 * Math.sin(gamma)
            - 0.014615 * Math.cos(2 * gamma) - 0.040849 * Math.sin(2 * gamma));
        double declination = 0.006918 - 0.399912 * Math.cos(gamma) + 0.070257 * Math.sin(gamma)
            - 0.006758 * Math.cos(2 * gamma) + 0.000907 * Math.sin(2 * gamma)
            - 0.002697 * Math.cos(3 * gamma) + 0.00148 * Math.sin(3 * gamma);
        double lat = Math.toRadians(latitude);
        double minute = utc.getHour() * 60 + utc.getMinute() + utc.getSecond() / 60.0;
        double solarMinute = ((minute + equation + 4 * longitude) % 1440 + 1440) % 1440;
        double hourAngle = Math.toRadians(solarMinute / 4 - 180);
        double cosine = Math.sin(lat) * Math.sin(declination) + Math.cos(lat) * Math.cos(declination) * Math.cos(hourAngle);
        sun.altitude = Math.toDegrees(Math.asin(clamp(cosine, -1, 1)));
        sun.azimuth = (Math.toDegrees(Math.atan2(Math.sin(hourAngle),
            Math.cos(hourAngle) * Math.sin(lat) - Math.tan(declination) * Math.cos(lat))) + 180 + 360) % 360;
        double riseCos = (Math.cos(Math.toRadians(90.833)) - Math.sin(lat) * Math.sin(declination))
            / (Math.cos(lat) * Math.cos(declination));
        sun.polarNight = riseCos > 1; sun.polarDay = riseCos < -1;
        LocalDate date = time.toLocalDate();
        double noon = 720 - 4 * longitude - equation;
        double halfDay = Math.toDegrees(Math.acos(clamp(riseCos, -1, 1))) * 4;
        // Pick the UTC midnight which produces a solar noon on this city's local date.
        ZonedDateTime midnight = date.atStartOfDay(ZoneOffset.UTC);
        ZonedDateTime solarNoon = midnight.plusSeconds(Math.round(noon * 60)).withZoneSameInstant(time.getZone());
        long shift = java.time.temporal.ChronoUnit.DAYS.between(solarNoon.toLocalDate(), date);
        midnight = midnight.plusDays(shift);
        sun.sunrise = midnight.plusSeconds(Math.round((noon - halfDay) * 60)).withZoneSameInstant(time.getZone());
        sun.sunset = midnight.plusSeconds(Math.round((noon + halfDay) * 60)).withZoneSameInstant(time.getZone());
        if (data != null && data.dates != null && Math.abs(data.latitude-latitude)<.0001
            && Math.abs(data.longitude-longitude)<.0001) {
            for (int i = 0; i < data.dates.length; i++) {
                if (date.toString().equals(data.dates[i]) && i < data.sunrise.length && i < data.sunset.length) {
                    try {
                        sun.sunrise = LocalDateTime.parse(data.sunrise[i]).atZone(time.getZone());
                        sun.sunset = LocalDateTime.parse(data.sunset[i]).atZone(time.getZone());
                    } catch (DateTimeException ignored) {}
                    break;
                }
            }
        }
        return sun;
    }

    public static double lunarPhase(Instant instant) {
        Instant reference = Instant.parse("2000-01-06T18:14:00Z");
        double cycles = Duration.between(reference, instant).getSeconds() / (29.530588853 * 86400);
        return cycles - Math.floor(cycles);
    }
    private static double clamp(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }
}
