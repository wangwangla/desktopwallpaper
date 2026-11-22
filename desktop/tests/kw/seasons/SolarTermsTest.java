package kw.seasons;

import kw.manager.core.seasons.SolarTerms;
import org.junit.Test;
import java.time.*;
import static org.junit.Assert.*;

public class SolarTermsTest {
    private final ZoneId zone=ZoneId.of("Asia/Hong_Kong");
    @Test public void known2026Calendar(){
        String[] expected={"02-04","02-18","03-05","03-20","04-05","04-20","05-05","05-21","06-05","06-21","07-07","07-23",
            "08-07","08-23","09-07","09-23","10-08","10-23","11-07","11-22","12-07","12-22","01-05","01-20"};
        for(int i=0;i<24;i++)assertEquals(SolarTerms.NAMES[i],LocalDate.parse("2026-"+expected[i]),SolarTerms.date(2026,i,zone));
    }
    @Test public void crossYearAndSeasonBoundaries(){
        assertEquals(21,SolarTerms.current(LocalDate.of(2026,1,1),zone));
        assertEquals(22,SolarTerms.current(LocalDate.of(2026,1,5),zone));
        assertEquals(23,SolarTerms.current(LocalDate.of(2026,2,3),zone));
        assertEquals(0,SolarTerms.current(LocalDate.of(2026,2,4),zone));
        assertEquals(19,SolarTerms.current(LocalDate.of(2026,11,22),zone));
        assertEquals(3,SolarTerms.season(22));
        assertEquals(LocalDate.of(2027,1,5),SolarTerms.next(LocalDate.of(2026,12,31),zone));
    }
    @Test public void allTermsAdvanceOnTheirLocalDayIncludingLeapYears(){
        for(int year:new int[]{2024,2026,2028})for(int i=0;i<24;i++){
            LocalDate day=SolarTerms.date(year,i,zone);
            assertEquals(i,SolarTerms.current(day,zone));
            assertEquals((i+23)%24,SolarTerms.current(day.minusDays(1),zone));
        }
    }
    @Test public void longitudeCrossingUsesTheConfiguredTimezone(){
        Instant spring=SolarTerms.instant(2026,0);
        assertTrue(spring.isAfter(Instant.parse("2026-02-03T19:00:00Z")));
        assertTrue(spring.isBefore(Instant.parse("2026-02-04T00:00:00Z")));
        assertEquals(LocalDate.of(2026,2,3),SolarTerms.date(2026,0,ZoneId.of("America/New_York")));
        assertEquals(LocalDate.of(2026,2,4),SolarTerms.date(2026,0,zone));
    }
}
