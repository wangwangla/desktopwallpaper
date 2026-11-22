package kw.muyu;

import kw.manager.core.muyu.MuyuProgress;
import org.junit.Test;
import java.nio.file.*;
import java.time.LocalDate;
import static org.junit.Assert.*;

public class MuyuProgressTest {
    @Test public void countsAndSettingsSurviveRestart() throws Exception {
        Path file=Files.createTempDirectory("muyu-test").resolve("progress.properties");MuyuProgress first=new MuyuProgress(file);
        first.hit(LocalDate.now());first.hit(LocalDate.now());first.sound=false;first.interval=.7f;first.save();
        MuyuProgress second=new MuyuProgress(file);assertEquals(2,second.total);assertEquals(2,second.today);assertFalse(second.sound);assertEquals(.7f,second.interval,.001f);
    }
    @Test public void nextDayKeepsLifetimeCount() throws Exception {
        MuyuProgress p=new MuyuProgress(Files.createTempDirectory("muyu-test").resolve("state"));p.hit(LocalDate.now());p.hit(LocalDate.now().plusDays(1));assertEquals(2,p.total);assertEquals(1,p.today);
    }
    @Test public void invalidSavedValuesAreClamped() throws Exception {
        Path file=Files.createTempDirectory("muyu-test").resolve("state");Files.write(file,("total=5\ntoday=900\ninterval=NaN\ndate="+LocalDate.now()+"\n").getBytes("UTF-8"));MuyuProgress p=new MuyuProgress(file);assertEquals(5,p.today);assertEquals(1.2f,p.interval,.001f);
    }
}
