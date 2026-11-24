package kw.wallpaper;

import org.junit.Test;
import javax.imageio.ImageIO;
import static org.junit.Assert.*;

public class WallpaperAssetsTest {
    private void image(String path)throws Exception {try(var stream=getClass().getResourceAsStream("/wallpapers/"+path)){assertNotNull(path,stream);var image=ImageIO.read(stream);assertNotNull(path,image);assertTrue(image.getWidth()>0&&image.getHeight()>0);}}
    @Test public void allSolarTermsAndMoonPhasesArePackagedAsPictures()throws Exception {
        for(int i=0;i<24;i++)image("seasons/term-"+i+".png");for(int i=0;i<32;i++)image("city/moon-"+i+".png");
    }
    @Test public void staticArtworkAndFontPagesExist()throws Exception {
        for(String path:new String[]{"muyu/background.png","muyu/fish.png","muyu/stick.png","muyu/pet-cat.png","muyu/pet-hamster.png","muyu/pet-panda.png","weather/cloud.png","weather/sun.png","weather/moon.png","ui/panel.png"})image(path);
        for(String font:new String[]{"body","numbers","heading"})try(var stream=getClass().getResourceAsStream("/wallpapers/fonts/"+font+".fnt")){
            assertNotNull(stream);String content=new String(stream.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
            var pages=java.util.regex.Pattern.compile("file=\"([^\"]+[.]png)\"").matcher(content);int count=0;while(pages.find()){image("fonts/"+pages.group(1));count++;}assertTrue(count>0);
        }
    }
}
