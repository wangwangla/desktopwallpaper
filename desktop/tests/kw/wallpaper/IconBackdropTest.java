package kw.wallpaper;

import org.junit.Test;
import com.badlogic.gdx.scenes.scene2d.Actor;
import kw.manager.core.DesktopIconSnapshot;
import kw.manager.core.demo.IconBackdropLayer;
import java.util.List;
import static org.junit.Assert.*;

public class IconBackdropTest {
    private DesktopIconSnapshot snapshot(int x){return new DesktopIconSnapshot(800,600,List.of(new DesktopIconSnapshot.Icon("a","A",x,40,64,80)),true,"");}
    @Test public void deletedIconsRemoveTheirActors(){IconBackdropLayer layer=new IconBackdropLayer(Actor::new);layer.synchronize(snapshot(20),800,600);assertEquals(1,layer.iconCount());Actor old=layer.backdrop("a");layer.synchronize(new DesktopIconSnapshot(800,600,List.of(),true,""),800,600);assertEquals(0,layer.iconCount());assertNull(old.getParent());}
    @Test public void iconMovementEasesToTheNewPosition(){IconBackdropLayer layer=new IconBackdropLayer(Actor::new);layer.synchronize(snapshot(20),800,600);Actor actor=layer.backdrop("a");assertEquals(12,actor.getX(),.01);layer.synchronize(snapshot(220),800,600);assertEquals(12,actor.getX(),.01);layer.act(.1f);assertTrue(actor.getX()>12&&actor.getX()<212);layer.act(.5f);assertEquals(212,actor.getX(),.01);assertSame(actor,layer.backdrop("a"));}
    @Test public void repeatedPollingDoesNotRestartEasing(){IconBackdropLayer layer=new IconBackdropLayer(Actor::new);layer.synchronize(snapshot(20),800,600);layer.synchronize(snapshot(220),800,600);for(int i=0;i<10;i++){layer.synchronize(snapshot(220),800,600);layer.act(.05f);}assertEquals(212,layer.backdrop("a").getX(),.01);}
    @Test public void unavailableReadsPreserveTheLastValidIcons(){IconBackdropLayer layer=new IconBackdropLayer(Actor::new);layer.synchronize(snapshot(20),800,600);layer.synchronize(DesktopIconSnapshot.pending("restarting"),800,600);assertEquals(1,layer.iconCount());}
    @Test public void physicalPixelsMapToWorldCoordinates(){IconBackdropLayer layer=new IconBackdropLayer(Actor::new);layer.synchronize(snapshot(20),1600,1200);assertEquals(24,layer.backdrop("a").getX(),.01);assertEquals(944,layer.backdrop("a").getY(),.01);assertEquals(160,layer.backdrop("a").getWidth(),.01);}
}
