package wk.com.test;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ScreenUtils;
import kw.joker.DynamicUtils;
import kw.manager.core.city.*;
import kw.seasons.*;
import kw.wallpaper.*;
import org.lwjgl.glfw.GLFW;
import javax.swing.SwingUtilities;
import java.awt.image.*;
import java.io.File;
import java.nio.ByteOrder;

@WallpaperDefinition(id="seasons",title="四季节气",description="二十四节气流转 · 树木倒影与晴雨云雪",accent=0x729579,
    thumbnail="wallpaper-previews/seasons.png",sceneTitle="四季节气 · 动态壁纸",controllerTitle="四季节气 · 壁纸控制器",executables={"SeasonsWallpaper.exe"})
public final class SeasonsLauncher {
    private Lwjgl3Application app;
    private volatile SeasonSettings settings;
    private WeatherService weather;
    private SeasonController controller;
    private volatile boolean attached;
    private WallpaperSession session;
    public static void main(String[] args){if(!WallpaperSession.forwardExisting("seasons",args))new SeasonsLauncher().run(args);}
    private void configure(SeasonSettings s){SeasonSettings old=settings;settings=s.copy();settings.save();
        if(old.latitude!=s.latitude||old.longitude!=s.longitude||!old.timezone.equals(s.timezone))weather.changingLocation(s);}
    private void attach(){try{DynamicUtils.makeWallpaper(app.getWindowHandle());attached=true;}
        catch(RuntimeException e){if(session!=null)throw e;SwingUtilities.invokeLater(()->javax.swing.JOptionPane.showMessageDialog(null,"桌面嵌入失败："+e.getMessage()));}}
    private void detach(){if(attached){DynamicUtils.destroyWallpaper(app.getWindowHandle());attached=false;}
        GLFW.glfwSetWindowSize(app.getWindowHandle(),1280,720);GLFW.glfwSetWindowPos(app.getWindowHandle(),60,60);GLFW.glfwShowWindow(app.getWindowHandle());}
    private void quit(){if(attached)detach();Gdx.app.exit();}
    private void run(String[] args){String snapshot=null;boolean wall=false;
        for(int i=0;i<args.length;i++){if("--wallpaper".equals(args[i]))wall=true;if("--snapshot".equals(args[i])&&i+1<args.length)snapshot=args[++i];}
        final String output=snapshot;final boolean initialWall=wall;
        Lwjgl3ApplicationConfiguration config=new Lwjgl3ApplicationConfiguration();config.setTitle("四季节气 · 动态壁纸");config.setWindowedMode(1280,720);
        config.setResizable(true);config.setForegroundFPS(15);config.setIdleFPS(15);config.disableAudio(true);config.setInitialVisible(output==null);
        app=new Lwjgl3Application();app.init(new ApplicationAdapter(){
            final SeasonArtwork artwork=new SeasonArtwork();SpriteBatch batch;Texture texture;Pixmap pixels;double elapsed;int frame,shot;
            final int[] terms={3,9,15,20,5,12,21,17};
            final String[] modes={"clear","clear","cloudy","snow","rain","clear","fog","overcast"};
            final String[] names={"spring-clear","summer-clear","autumn-cloudy","winter-snow","spring-rain","autumn-night","winter-fog","autumn-overcast"};
            public void create(){settings=output==null?SeasonSettings.loadSeason():new SeasonSettings();
                weather=new WeatherService(()->settings,output==null);batch=new SpriteBatch();pixels=new Pixmap(SeasonArtwork.W,SeasonArtwork.H,Pixmap.Format.RGBA8888);
                pixels.getPixels().order(ByteOrder.BIG_ENDIAN);texture=new Texture(pixels);texture.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);
                if(output!=null)preview();else SwingUtilities.invokeLater(()->controller=new SeasonController(()->settings,SeasonsLauncher.this::configure,weather,SeasonsLauncher.this::attach,SeasonsLauncher.this::detach,SeasonsLauncher.this::quit));
                if(initialWall&&output==null)Gdx.app.postRunnable(SeasonsLauncher.this::attach);
                if(output==null)try{
                    session=new WallpaperSession("seasons",()->attached?"wallpaper":"preview",Gdx.app::postRunnable,command->{
                        switch(command){case "wallpaper"->attach();case "preview"->detach();case "controls"->SwingUtilities.invokeLater(()->{if(controller!=null)controller.show();});case "stop"->quit();}
                    });
                }catch(java.io.IOException e){throw new RuntimeException(e);}
            }
            private void preview(){settings.previewTerm=terms[shot];settings.previewMinutes=shot==5?1320:720;settings.weatherMode=modes[shot];
                WeatherData example=new WeatherData();example.temperature=new float[]{18,30,21,-2,18,22,3,16}[shot];weather.data=example;elapsed=5;}
            public void render(){elapsed+=Math.min(Gdx.graphics.getDeltaTime(),.15f);
                SeasonSettings s=settings;WeatherData data=weather.data.preview(s.weatherMode);
                BufferedImage image=artwork.frame(s,data,weather.status,elapsed);
                int[] rgb=((DataBufferInt)image.getRaster().getDataBuffer()).getData();java.nio.IntBuffer buffer=pixels.getPixels().asIntBuffer();
                for(int pixel:rgb)buffer.put((pixel<<8)|255);texture.draw(pixels,0,0);
                Gdx.gl.glClearColor(0,0,0,1);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                batch.getProjectionMatrix().setToOrtho2D(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());batch.begin();
                // Fill every monitor aspect ratio without stretching the illustration.
                float scale=Math.max(Gdx.graphics.getWidth()/(float)SeasonArtwork.W,Gdx.graphics.getHeight()/(float)SeasonArtwork.H);
                float w=SeasonArtwork.W*scale,h=SeasonArtwork.H*scale;batch.draw(texture,(Gdx.graphics.getWidth()-w)/2,(Gdx.graphics.getHeight()-h)/2,w,h);batch.end();
                if(output!=null&&++frame>=3){Pixmap capture=ScreenUtils.getFrameBufferPixmap(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());
                    File file=new File(output,names[shot]+".png");file.getParentFile().mkdirs();PixmapIO.writePNG(new FileHandle(file),capture,-1,true);capture.dispose();System.out.println("Saved "+file);
                    frame=0;if(++shot==names.length)Gdx.app.exit();else preview();}
                if(output==null&&Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){if(attached)detach();else quit();}
                if(output==null&&Gdx.input.isKeyJustPressed(Input.Keys.F1)&&controller!=null)SwingUtilities.invokeLater(controller::show);
            }
            public void dispose(){if(session!=null)session.close();if(attached)detach();weather.close();if(controller!=null)controller.dispose();texture.dispose();pixels.dispose();batch.dispose();}
        },config);System.exit(0);
    }
}
