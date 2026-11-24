package kw.wallpaper;

import org.junit.Test;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

public class WallpaperControlTest {
    @Test public void discoversOnlyActualWallpaperLaunchers()throws Exception {
        var entries=WallpaperCatalog.discover().stream().filter(WallpaperCatalog.Entry::builtIn).toList();
        assertEquals(Set.of("muyu","seasons","weather-city","demo"),new HashSet<>(entries.stream().map(WallpaperCatalog.Entry::id).toList()));
        for(var entry:entries)assertNotNull(Class.forName(entry.mainClass()).getMethod("main",String[].class));
        assertFalse(entries.stream().anyMatch(e->e.mainClass().endsWith("WallManagerLauncher")));
    }
    @Test public void sessionsControlTheRendererAndRejectWrongTokens()throws Exception {
        String id="test-"+UUID.randomUUID();AtomicReference<String> mode=new AtomicReference<>("preview");
        try(WallpaperSession host=new WallpaperSession(id,mode::get,Runnable::run,command->{if(command.equals("wallpaper"))mode.set("wallpaper");if(command.equals("preview"))mode.set("preview");})) {
            var endpoint=WallpaperSession.endpoint(id).orElseThrow();
            assertEquals("preview",WallpaperSession.request(endpoint,"status"));
            assertEquals("ok",WallpaperSession.request(endpoint,"wallpaper"));assertEquals("wallpaper",WallpaperSession.request(endpoint,"status"));
            assertEquals("ok",WallpaperSession.request(endpoint,"preview"));assertEquals("preview",WallpaperSession.request(endpoint,"status"));
            try{WallpaperSession.request(new WallpaperSession.Endpoint(endpoint.pid(),endpoint.started(),endpoint.port(),"wrong"),"stop");fail("Unauthorized command accepted");}catch(IOException expected){assertTrue(expected.getMessage().contains("拒绝"));}
            assertEquals("preview",WallpaperSession.request(endpoint,"status"));
        }
        assertTrue(WallpaperSession.endpoint(id).isEmpty());
    }
    @Test public void duplicateInstancesCannotReplaceTheRunningRegistration()throws Exception {
        String id="test-"+UUID.randomUUID();
        try(WallpaperSession first=new WallpaperSession(id,()->"preview",Runnable::run,c->{})){
            var original=WallpaperSession.endpoint(id).orElseThrow();
            try(WallpaperSession ignored=new WallpaperSession(id,()->"preview",Runnable::run,c->{})){fail("Duplicate instance accepted");}catch(IOException expected){}
            assertEquals(original,WallpaperSession.endpoint(id).orElseThrow());
            assertEquals("preview",WallpaperSession.request(original,"status"));
        }
    }
    @Test public void errorsAreReturnedWithoutChangingState()throws Exception {
        String id="test-"+UUID.randomUUID();
        try(WallpaperSession host=new WallpaperSession(id,()->"preview",Runnable::run,c->{throw new IllegalStateException("test attach failure");})){
            var endpoint=WallpaperSession.endpoint(id).orElseThrow();
            try{WallpaperSession.request(endpoint,"wallpaper");fail("Failure hidden");}catch(IOException expected){assertEquals("test attach failure",expected.getMessage());}
            assertEquals("preview",WallpaperSession.request(endpoint,"status"));
        }
    }
}
