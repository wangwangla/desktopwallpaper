package wk.com.test;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.MemoryUtil;

/**
 * @Auther jian xian si qi
 * @Date 2024/1/18 21:14
 */
class App {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        // Configure FPS
        config.setForegroundFPS(30);
        config.setIdleFPS(30);
        // Configure window layout
        config.setDecorated(false);
        config.setResizable(false);
        config.setWindowedMode(100, 100);
        config.setWindowPosition(0, 0);
        // Configure window title
        final String TITLE = "xxxxxxxxx";
        config.setTitle(TITLE);
        // Configure window display
        config.setInitialVisible(true);
        config.setTransparentFramebuffer(true);
        config.setInitialBackgroundColor(new Color(0,0,0,0));
//            User32.INSTANCE.SystemParametersInfoA(SPI_SETDESKWALLPAPER, 0, null, SPIF_UPDATEINIFILE | SPIF_SENDWININICHANGE);
        // Handle GLFW error
        GLFW.glfwSetErrorCallback(new GLFWErrorCallback() {
            @Override
            public void invoke(int error, long description) {
                if (error != GLFW.GLFW_NO_ERROR) {
                    String descriptionString = MemoryUtil.memUTF8(description);
//                    Logger.error("System", "Detected a GLFW error: (Code " + error + ") " + descriptionString);
                }
            }
        });
        // Instantiate the App
        Lwjgl3Application app = new Lwjgl3Application();
        app.init(new Game() {
            @Override
            public void create() {

            }
        }, config);
    }
}
