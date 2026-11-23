package kw.muyu;

import kw.manager.core.muyu.MuyuStroke;
import org.junit.Test;
import static org.junit.Assert.*;

public class MuyuStrokeTest {
    @Test public void contactIsNotDelayedByFloatRounding(){
        MuyuStroke stroke=new MuyuStroke();stroke.request();int contacts=0;
        for(int i=0;i<8;i++)contacts+=stroke.update(.02f);
        assertEquals(1,contacts);assertEquals(0,stroke.update(.02f));
    }
    @Test public void meritWaitsUntilContactAndFiresOnce(){
        MuyuStroke stroke=new MuyuStroke();stroke.request();assertEquals(0,stroke.update(.1f));
        assertEquals(1,stroke.update(.08f));assertEquals(0,stroke.update(.5f));
    }
    @Test public void rapidClicksEachGetAContact(){
        MuyuStroke stroke=new MuyuStroke();stroke.request();stroke.request();stroke.request();
        assertEquals(3,stroke.update(1.1f));assertEquals(0,stroke.update(1));
    }
    @Test public void smallFramesAndLongFramesProduceSameCount(){
        MuyuStroke small=new MuyuStroke(),large=new MuyuStroke();for(int i=0;i<7;i++){small.request();large.request();}
        int contacts=0;for(int i=0;i<180;i++)contacts+=small.update(1/60f);
        assertEquals(7,contacts);assertEquals(contacts,large.update(3));
    }
}
