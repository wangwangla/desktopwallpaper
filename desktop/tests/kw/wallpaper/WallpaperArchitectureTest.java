package kw.wallpaper;

import org.junit.Test;
import com.kw.gdx.BaseGame;
import com.kw.gdx.screen.BaseScreen;
import kw.manager.core.WallpaperGame;
import kw.manager.core.muyu.*;
import kw.manager.core.city.*;
import kw.manager.core.seasons.*;
import static org.junit.Assert.*;

public class WallpaperArchitectureTest {
    @Test public void allWallpapersUseTheExistingGameAndScreenBases(){
        for(Class<?> game:new Class<?>[]{MuyuGame.class,WeatherCityGame.class,SeasonsGame.class})assertTrue(BaseGame.class.isAssignableFrom(game));
        for(Class<?> screen:new Class<?>[]{MuyuScreen.class,WeatherCityScreen.class,SeasonsScreen.class})assertEquals(BaseScreen.class,screen.getSuperclass());
    }
    @Test public void renderersAndBusinessSettingsBelongToCore(){
        for(Class<?> type:new Class<?>[]{MuyuGame.class,WeatherCityGame.class,SeasonsGame.class,SeasonSettings.class,kw.manager.core.WallpaperAssets.class})assertTrue(type.getPackageName().startsWith("kw.manager.core"));
        assertEquals(BaseGame.class,WallpaperGame.class.getSuperclass());
    }
    @Test public void screensUseBaseScreensStageRendering(){
        for(Class<?> screen:new Class<?>[]{MuyuScreen.class,WeatherCityScreen.class,SeasonsScreen.class}){
            assertFalse(java.util.Arrays.stream(screen.getDeclaredMethods()).anyMatch(m->m.getName().equals("render")));
            assertFalse(java.util.Arrays.stream(screen.getDeclaredFields()).anyMatch(f->f.getType()==com.badlogic.gdx.graphics.g2d.SpriteBatch.class));
        }
    }
}
