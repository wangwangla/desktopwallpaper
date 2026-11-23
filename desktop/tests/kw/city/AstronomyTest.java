package kw.city;

import kw.manager.core.city.*;
import org.junit.Test;
import java.time.*;
import static org.junit.Assert.*;

public class AstronomyTest {
    private ZonedDateTime hongKong(int hour) {
        return ZonedDateTime.of(2026,11,22,hour,0,0,0,ZoneId.of("Asia/Hong_Kong"));
    }
    @Test public void solarAltitudeFollowsTheActualLocalHour() {
        assertTrue(Astronomy.sun(hongKong(12),22.3193,114.1694,null).altitude>35);
        assertTrue(Astronomy.sun(hongKong(1),22.3193,114.1694,null).altitude<0);
    }
    @Test public void forecastSunTimesOverrideTheOfflineEstimate() {
        WeatherData data=new WeatherData();data.dates=new String[]{"2026-11-22"};
        data.latitude=22.3193;data.longitude=114.1694;
        data.sunrise=new String[]{"2026-11-22T06:39"};data.sunset=new String[]{"2026-11-22T17:38"};
        Astronomy.Sun sun=Astronomy.sun(hongKong(12),22.3193,114.1694,data);
        assertEquals(LocalTime.of(6,39),sun.sunrise.toLocalTime());
        assertEquals(LocalTime.of(17,38),sun.sunset.toLocalTime());
    }
    @Test public void offlineSunTimesRemainOnTheCorrectDateAcrossTheDateLine() {
        ZonedDateTime time=ZonedDateTime.of(2026,11,22,12,0,0,0,ZoneId.of("Pacific/Kiritimati"));
        Astronomy.Sun sun=Astronomy.sun(time,1.872,-157.427,null);
        assertEquals(time.toLocalDate(),sun.sunrise.toLocalDate());
        assertEquals(time.toLocalDate(),sun.sunset.toLocalDate());
        assertTrue(sun.sunrise.isBefore(sun.sunset));
    }
    @Test public void polarDayAndNightDoNotProduceInvalidAngles() {
        ZoneId zone=ZoneId.of("Europe/Oslo");
        Astronomy.Sun summer=Astronomy.sun(ZonedDateTime.of(2026,6,21,12,0,0,0,zone),69.649,18.955,null);
        Astronomy.Sun winter=Astronomy.sun(ZonedDateTime.of(2026,12,21,12,0,0,0,zone),69.649,18.955,null);
        assertTrue(summer.polarDay);assertTrue(winter.polarNight);
        assertFalse(Double.isNaN(summer.altitude));assertFalse(Double.isNaN(winter.altitude));
    }
    @Test public void moonPhaseMatchesTheReferenceAndHalfCycle() {
        Instant reference=Instant.parse("2000-01-06T18:14:00Z");
        assertEquals(0,Astronomy.lunarPhase(reference),.001);
        assertEquals(.5,Astronomy.lunarPhase(reference.plusSeconds(Math.round(29.530588853*86400/2))),.001);
    }
    @Test public void manualWeatherPreviewDoesNotOverwriteRealConditions() {
        WeatherData real=new WeatherData();real.cloudCover=68;real.precipitation=0;
        WeatherData rain=real.preview("rain");
        assertTrue(rain.precipitation>0);assertEquals(100,rain.cloudCover,.01);
        assertEquals(68,real.cloudCover,.01);assertEquals(0,real.precipitation,.01);
    }
    @Test public void defaultAndShadowShadersAreBundled() {
        assertNotNull(getClass().getResourceAsStream("/com/badlogic/gdx/graphics/g3d/shaders/default.vertex.glsl"));
        assertNotNull(getClass().getResourceAsStream("/com/badlogic/gdx/graphics/g3d/shaders/depth.fragment.glsl"));
    }
    @Test public void aPreviousCitysForecastDoesNotOverrideTheNewCitysSunTimes() {
        WeatherData hongKong=new WeatherData();hongKong.latitude=22.3193;hongKong.longitude=114.1694;
        hongKong.dates=new String[]{"2026-11-22"};hongKong.sunrise=new String[]{"2026-11-22T06:39"};
        hongKong.sunset=new String[]{"2026-11-22T17:38"};
        ZonedDateTime london=ZonedDateTime.of(2026,11,22,12,0,0,0,ZoneId.of("Europe/London"));
        Astronomy.Sun sun=Astronomy.sun(london,51.507,-.127,hongKong);
        assertTrue(sun.sunrise.getHour()>=7);assertTrue(sun.sunrise.getHour()<=8);
    }
}
