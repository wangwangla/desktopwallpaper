package kw.wallpaper;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.math.Vector2;
import com.kw.gdx.screen.BaseScreen;
import kw.manager.core.*;
import kw.manager.core.muyu.*;
import org.lwjgl.glfw.GLFW;

/** A real GL/Stage smoke check, using Actor positions rather than reproducing hit-box logic. */
public final class StageInputProbe {
    private static Lwjgl3Application app;
    private static Stage stage;
    private static MuyuScreen screen;
    private static long before;
    private static int frames,settingsOpened;
    private static Throwable failure;
    private static void click(String name){
        Actor actor=stage.getRoot().findActor(name);if(actor==null)throw new AssertionError("Missing Actor: "+name);
        Vector2 point=actor.localToStageCoordinates(new Vector2(actor.getWidth()/2,actor.getHeight()/2));
        stage.stageToScreenCoordinates(point);screen.desktopTouch(Math.round(point.x),Math.round(point.y),true);screen.desktopTouch(Math.round(point.x),Math.round(point.y),false);
    }
    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config=new Lwjgl3ApplicationConfiguration();config.setWindowedMode(1200,800);config.setInitialVisible(false);config.disableAudio(true);config.setForegroundFPS(60);
        app=new Lwjgl3Application();
        app.init(new MuyuGame(new WallpaperPlatform(){
            public void wallpaper(){}public void preview(){}public void quit(){Gdx.app.exit();}public void showControls(){}public void showWorkControls(){settingsOpened++;}
            public void ready(WallpaperGame<?> game){try{
                screen=((MuyuGame)game).wallpaperScreen();var field=BaseScreen.class.getDeclaredField("stage");field.setAccessible(true);stage=(Stage)field.get(screen);
                if(Gdx.input.getInputProcessor()!=stage)throw new AssertionError("Input must be handled by BaseScreen Stage");
                before=screen.progress.total;click("muyu");
            }catch(Throwable e){failure=e;Gdx.app.exit();}}
            public void frame(WallpaperGame<?> game){try{
                frames++;
                if(frames==25){
                    if(screen.progress.total!=before+1)throw new AssertionError("Actor click failed");
                    click("work-settings");if(settingsOpened!=1)throw new AssertionError("Settings callback failed");
                    click("work-pause");click("work-pet");
                    if(screen.progress.total!=before+1)throw new AssertionError("Card click also hit wood");
                    GLFW.glfwSetWindowSize(app.getWindowHandle(),1500,900);
                }
                if(frames==40)click("muyu");
                if(frames==65){if(screen.progress.total!=before+2)throw new AssertionError("Resized Actor click failed");System.out.println("PASS: Stage click, card callbacks, resize and input routing");Gdx.app.exit();}
            }catch(Throwable e){failure=e;Gdx.app.exit();}}
        },false,null),config);
        if(failure!=null){failure.printStackTrace();System.exit(1);}System.exit(0);
    }
}
