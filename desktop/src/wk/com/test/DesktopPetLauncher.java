package wk.com.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.graphics.Color;

import org.lwjgl.glfw.GLFW;

import kw.manager.core.PetGame;

public class DesktopPetLauncher {
    private Lwjgl3Application app;
    public static void main(String[] args) {
        DesktopPetLauncher launcher = new DesktopPetLauncher();
        launcher.run();
    }


    public long run(){

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        // Configure FPS
        config.setForegroundFPS(30);
        config.setIdleFPS(30);
        // Configure window layout
        config.setDecorated(true);
        config.setResizable(false);
        config.setWindowedMode((int) (1920 * 0.5f), (int) (1200 * 0.5f));
        config.setWindowPosition(0, 100);
        // Configure window title
        final String TITLE = "xx";
        config.useOpenGL3(true, 3, 2);
        config.setTitle(TITLE);
        config.setInitialVisible(true);
        config.setTransparentFramebuffer(true);
        config.setInitialBackgroundColor(new Color(0, 0, 0, 0));

        // Instantiate the App
        this.app = new Lwjgl3Application();

        config.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override
            public void created(Lwjgl3Window window) {
                System.out.println("Window created!");
// ---- 关键：延迟 1 帧再绑定 (否则 handle 为 0) ----
                Gdx.app.postRunnable(() -> {
                    long h = window.getWindowHandle();
                    System.out.println("Window handle = " + h);
                    GLFW.glfwSetWindowAttrib(h, GLFW.GLFW_FLOATING, GLFW.GLFW_TRUE);
                    drag();
                });
            }
        });

        app.init(new PetGame(), config);
        return app.getWindowHandle();
    }

    boolean dragging = false;
    double dragOffsetX, dragOffsetY;
    public void drag(){
        long window = app.getWindowHandle();
        // 鼠标移动
        GLFW.glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            if (dragging) {
                // 获取屏幕位置
                int[] winX = new int[1];
                int[] winY = new int[1];
                GLFW.glfwGetWindowPos(window, winX, winY);

                // 新位置 = 旧窗口位置 + 鼠标移动差
                int newX = (int)(winX[0] + xpos - dragOffsetX);
                int newY = (int)(winY[0] + ypos - dragOffsetY);

                GLFW.glfwSetWindowPos(window, newX, newY);
            }
        });

        // 鼠标按下
        GLFW.glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            System.out.println("========================");
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                if (action == GLFW.GLFW_PRESS) {
                    dragging = true;
                    System.out.println("========================");

                    double[] xpos = new double[1];
                    double[] ypos = new double[1];
                    GLFW.glfwGetCursorPos(win, xpos, ypos);

                    // 记录鼠标按下时窗口相对位置
                    dragOffsetX = xpos[0];
                    dragOffsetY = ypos[0];
                } else if (action == GLFW.GLFW_RELEASE) {
                    dragging = false;
                }
            }
        });

    }
}
