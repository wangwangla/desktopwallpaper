package kw.wallpaper;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import java.io.IOException;
import static org.junit.Assert.*;

public class WallpaperAutostartTest {
    @Rule public TemporaryFolder temp=new TemporaryFolder();
    private static class Entry implements WallpaperAutostart.StartupEntry {
        String command;boolean fail;
        public void write(String command)throws IOException {if(fail)throw new IOException("registry unavailable");this.command=command;}
        public void remove(){command=null;}
        public boolean exists(){return command!=null;}
    }
    private WallpaperAutostart store(Entry entry){return new WallpaperAutostart(temp.getRoot().toPath().resolve("startup.properties"),entry,()->"\"C:\\Wallpaper app\\DesktopWallpaperManager.exe\" --restore-wallpaper");}
    @Test public void noWallpaperDoesNotRegisterStartup(){Entry e=new Entry();var store=store(e);assertFalse(store.enabled());assertEquals("",store.selected());assertNull(e.command);}
    @Test public void settingWallpaperPersistsItAndSwitchingReplacesIt()throws Exception {
        Entry e=new Entry();var store=store(e);store.enable("muyu");assertTrue(store.enabled());assertEquals("muyu",store(e).selected());
        assertEquals("\"C:\\Wallpaper app\\DesktopWallpaperManager.exe\" --restore-wallpaper",e.command);
        store.enable("seasons");assertEquals("seasons",store(e).selected());assertTrue(store.enabled());
    }
    @Test public void stoppingAnotherPreviewDoesNotDisableCurrentWallpaper()throws Exception {
        Entry e=new Entry();var store=store(e);store.enable("seasons");store.disable("muyu");assertEquals("seasons",store.selected());assertTrue(store.enabled());
    }
    @Test public void stoppingCurrentWallpaperOrAllRemovesStartup()throws Exception {
        Entry e=new Entry();var store=store(e);store.enable("seasons");store.disable("seasons");assertFalse(store.enabled());assertEquals("",store.selected());assertNull(e.command);
        store.enable("muyu");store.disable(null);assertFalse(store.enabled());assertNull(e.command);
    }
    @Test public void failedRegistrationDoesNotForgetPreviousSelection()throws Exception {
        Entry e=new Entry();var store=store(e);store.enable("seasons");e.fail=true;
        try{store.enable("muyu");fail("Expected registry failure");}catch(IOException expected){}
        assertEquals("seasons",store.selected());assertTrue(store.enabled());
    }
}
