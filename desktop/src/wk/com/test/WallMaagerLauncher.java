package wk.com.test;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;

import kw.manager.core.WindowManagerGame;
import kw.manager.core.listener.ManagerListener;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/5 20:50
 */
public class WallMaagerLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        // Configure FPS

        config.setDecorated(true);
        config.setResizable(false);
        config.setWindowedMode(500, 500);
        config.setWindowPosition(0, 100);
        // Configure window title
        final String TITLE = "xxxxxxxxx";
        config.setTitle(TITLE);
        config.setInitialVisible(true);
        config.setTransparentFramebuffer(true);
        config.setInitialBackgroundColor(new Color(0,0,0,0));
        // Instantiate the App
        Lwjgl3Application app = new Lwjgl3Application();
        app.init(new WindowManagerGame(new ManagerListener() {
            @Override
            public void windowForward() {

            }

            @Override
            public void moveWindowPosition(float x, float y) {

            }
        }), config);
    }
}
