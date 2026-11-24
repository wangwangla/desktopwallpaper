package kw.manager.core;

import com.kw.gdx.asset.Asset;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import java.util.*;

/** Screen-scoped references to resources loaded and released by the existing Asset singleton. */
public final class WallpaperAssets implements AutoCloseable {
    private static final Map<String,Integer> USERS=new HashMap<>();
    private final Set<String> acquired=new LinkedHashSet<>();
    private void retain(String path){if(acquired.add(path))USERS.merge(path,1,Integer::sum);}
    public Texture texture(String name){return texturePath("wallpapers/"+name);}
    public Texture texturePath(String path){path=path.replace((char)92,'/');Texture texture=Asset.getAsset().getTexture(path);
        if(texture==null)throw new IllegalStateException("Missing wallpaper resource: "+path);retain(path);return texture;}
    public BitmapFont font(boolean numbers){return font(numbers?"numbers":"body");}
    private BitmapFont font(String name){String path="wallpapers/fonts/"+name+".fnt";BitmapFont font=Asset.getAsset().loadBitFont(path);for(var region:font.getRegions()){Texture texture=region.getTexture();if(texture.getMinFilter()!=Texture.TextureFilter.Linear||texture.getMagFilter()!=Texture.TextureFilter.Linear)texture.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);}retain(path);return font;}
    public Image image(String name){Image image=new Image(texture(name));image.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);return image;}
    public Label label(String text,float size,int rgb){return label(text,size,rgb,false);}
    public Label label(String text,float size,int rgb,boolean numbers){String face=numbers?"numbers":size>=48?"heading":"body";Label label=new Label(text,new Label.LabelStyle(font(face),Color.WHITE));label.setColor(color(rgb));label.setFontScale(size/(numbers?88f:face.equals("heading")?64f:32f));label.setAlignment(Align.left);label.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);return label;}
    public static Color color(int rgb){return new Color((rgb<<8)|255);}
    @Override public void close(){for(String path:acquired){int count=USERS.get(path)-1;if(count==0){USERS.remove(path);Asset.getAsset().unloadResource(path);}else USERS.put(path,count);}acquired.clear();}
}
