package wk.com.test;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeWin32;

import kw.manager.core.WindowGame;
import kw.manager.core.WindowManagerGame;
import kw.manager.core.listener.ManagerListener;
import kw.manager.core.listener.WindowListener;
import kw.test.DynamicUtils;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/18 21:14
 */
class WallpapgerLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        // Configure FPS
        config.setForegroundFPS(30);
        config.setIdleFPS(30);
        // Configure window layout
        config.setDecorated(false);
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
        app.init(new WindowGame(new WindowListener(){
            @Override
            public void windowForward() {
//                long windowHandle = app.getWindowHandle();
//                GLFW.glfwSetWindowAttrib(windowHandle,GLFW.GLFW_FLOATING,GLFW.GLFW_TRUE);
            }

            @Override
            public void moveWindowPosition(float x, float y) {
                long windowHandle = app.getWindowHandle();
                int windowX[] = new int[1];
                int windowY[] = new int[1];
                GLFW.glfwGetWindowPos(windowHandle, windowX, windowY);
                GLFW.glfwFocusWindow(windowHandle);
                DynamicUtils.makeWallpaper(windowHandle);
            }
        }){
            @Override
            public void render() {
                super.render();
            }
        }, config);
    }
}
