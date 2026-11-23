package kw.muyu;

import kw.manager.core.muyu.*;
import org.junit.Test;
import java.time.*;
import java.nio.file.*;
import static org.junit.Assert.*;

public class WorkScheduleTest {
    private final WorkSettings s=new WorkSettings();
    @Test public void lunchDoesNotEarnAndDailyIncomeCaps(){
        WorkSchedule.Snapshot lunch=WorkSchedule.at(s,LocalDateTime.parse("2026-11-23T12:45:00"),0,false);assertEquals("午间休息",lunch.state);assertEquals(80,lunch.earned,.00001);assertEquals(0,lunch.speed,0);assertEquals(27000,lunch.planned);
        assertEquals(200,WorkSchedule.at(s,LocalDateTime.parse("2026-11-23T20:00:00"),0,false).earned,0);
    }
    @Test public void weekendsStayAtZeroAndFindNextShift(){
        WorkSchedule.Snapshot rest=WorkSchedule.at(s,LocalDateTime.parse("2026-11-22T18:00:00"),0,false);assertEquals("休息时间",rest.state);assertEquals(0,rest.earned,0);assertEquals(15*3600,rest.countdown);assertEquals(0,rest.restDays);
    }
    @Test public void paydayClampsToEndOfShortMonth(){
        s.payday=31;assertEquals(0,WorkSchedule.at(s,LocalDateTime.parse("2026-04-30T10:00:00"),0,false).paydayDays);
        assertEquals(27,WorkSchedule.at(s,LocalDateTime.parse("2026-02-01T10:00:00"),0,false).paydayDays);
    }
    @Test public void monthEstimateUsesOnlyEarlierWorkingDays(){
        assertEquals(280,WorkSchedule.at(s,LocalDateTime.parse("2026-11-03T12:00:00"),0,false).monthly,.00001);
    }
    @Test public void pausedTimeSurvivesRestartAndExcludesOnlyProductiveMinutes() throws Exception{
        Path file=Files.createTempDirectory("work-pause-test").resolve("state");WorkTracker t=new WorkTracker(file);t.toggle(LocalDateTime.parse("2026-11-23T11:30:00"));
        WorkTracker restarted=new WorkTracker(file);WorkSchedule.Snapshot paused=restarted.snapshot(s,LocalDateTime.parse("2026-11-23T13:45:00"));assertEquals(66.6666667,paused.earned,.00001);assertEquals(0,paused.speed,0);
        restarted.toggle(LocalDateTime.parse("2026-11-23T13:45:00"));assertEquals(73.3333333,restarted.snapshot(s,LocalDateTime.parse("2026-11-23T14:00:00")).earned,.00001);
        assertFalse(restarted.snapshot(s,LocalDateTime.parse("2026-11-24T10:00:00")).paused);
    }
    @Test public void refusesSchedulesWithNoProductiveTime(){s.breakStart=s.start;s.breakEnd=s.end;try{s.validate();fail("Expected validation");}catch(IllegalArgumentException expected){}}
}
