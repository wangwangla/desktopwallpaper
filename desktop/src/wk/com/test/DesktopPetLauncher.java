package wk.com.test;

import static com.sun.jna.platform.win32.WinUser.GWL_EXSTYLE;
import static com.sun.jna.platform.win32.WinUser.SWP_FRAMECHANGED;
import static com.sun.jna.platform.win32.WinUser.SWP_NOMOVE;
import static com.sun.jna.platform.win32.WinUser.SWP_NOSIZE;
import static org.lwjgl.system.windows.User32.SWP_NOZORDER;
import static org.lwjgl.system.windows.User32.WS_EX_CLIENTEDGE;
import static org.lwjgl.system.windows.User32.WS_EX_DLGMODALFRAME;
import static org.lwjgl.system.windows.User32.WS_EX_STATICEDGE;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.graphics.Color;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;

import org.lwjgl.glfw.GLFW;

import java.util.Scanner;

import kw.manager.core.PetPetGame;
import kw.manager.core.listener.MoveListener;
import kw.test.DynamicUtils;

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
        config.setDecorated(false);
        config.setResizable(false);

        Graphics.DisplayMode displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
        int screenWidth = displayMode.width;



        config.setWindowedMode((int) (screenWidth * 0.2f), (int) (screenWidth * 0.2f));
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
                    //置顶
                    GLFW.glfwSetWindowAttrib(h, GLFW.GLFW_FLOATING, GLFW.GLFW_TRUE);
                    drag();
                });
            }
        });
        app.setMouseMoveListener(new MoveListener(){
            @Override
            public void movePosition(float x, float y) {
                long window = app.getWindowHandle();
                int[] winX = new int[1];
                int[] winY = new int[1];
                GLFW.glfwGetWindowPos(window, winX, winY);
                // 新位置 = 旧窗口位置 + 鼠标移动差
                int newX = (int)(winX[0] + x - dragOffsetX);
                int newY = (int)(winY[0] + y - dragOffsetY);
                GLFW.glfwSetWindowPos(window, newX, newY);
            }

            @Override
            public void startPosition(float x, float y) {
                dragOffsetX = x;
                dragOffsetY = y;
            }
        });
        new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (sc.hasNextLine()) {
                if (sc.nextLine().equals("EXIT")) {
                    DynamicUtils.destroyWallpaper(app.getWindowHandle());
                    System.out.println("Wallpaper cleaned.");
                    System.exit(0);
                }
            }
        }).start();
        app.init(new PetPetGame(), config);
        return app.getWindowHandle();
    }



    private static void removeWindowShadow(WinDef.HWND hwnd) {
        // 1. 去掉 DWM 阴影
        final int DWMWA_NCRENDERING_POLICY = 2;
        final int DWMNCRP_DISABLED = 0;
        Dwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_NCRENDERING_POLICY, new int[]{DWMNCRP_DISABLED}, 4);

        // 2. 去掉系统边框阴影
        int style = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_STYLE);
        style &= ~(WinUser.WS_CAPTION | WinUser.WS_THICKFRAME); // 去掉标题栏和可调整边框
        User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_STYLE, style);

        int exStyle = User32.INSTANCE.GetWindowLong(hwnd, GWL_EXSTYLE);
        exStyle &= ~(WS_EX_DLGMODALFRAME | WS_EX_CLIENTEDGE | WS_EX_STATICEDGE); // 去掉额外边框
        User32.INSTANCE.SetWindowLong(hwnd, GWL_EXSTYLE, exStyle);

        // 更新窗口，让修改生效
        User32.INSTANCE.SetWindowPos(hwnd, null, 0, 0, 0, 0,
                SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    }

    // DWM API，用于取消 Aero 阴影
    public interface Dwmapi extends com.sun.jna.Library {
        Dwmapi INSTANCE = Native.load("dwmapi", Dwmapi.class);

        int DwmSetWindowAttribute(WinDef.HWND hwnd, int dwAttribute, IntByReference pvAttribute, int cbAttribute);
        int DwmSetWindowAttribute(WinDef.HWND hwnd, int dwAttribute, int[] pvAttribute, int cbAttribute);
    }

    public static void removeShadow(long hwndPointer) {
        WinDef.HWND hwnd = new WinDef.HWND(Pointer.createConstant(hwndPointer));
        User32.INSTANCE.SetWindowLong(hwnd, GWL_EXSTYLE, WinUser.WS_EX_LAYERED);
//        Dwmapi.DwmExtendFrameIntoClientArea(hwnd, new MARGINS(-1));
    }

    boolean dragging = false;
    double dragOffsetX, dragOffsetY;
    public void drag(){
        long window = app.getWindowHandle();
        // 鼠标移动
//        GLFW.glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
//            if (dragging) {
//                // 获取屏幕位置
//                int[] winX = new int[1];
//                int[] winY = new int[1];
//                GLFW.glfwGetWindowPos(window, winX, winY);
//
//                // 新位置 = 旧窗口位置 + 鼠标移动差
//                int newX = (int)(winX[0] + xpos - dragOffsetX);
//                int newY = (int)(winY[0] + ypos - dragOffsetY);
//
//                GLFW.glfwSetWindowPos(window, newX, newY);
//            }
//        });

        // 鼠标按下
//        GLFW.glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
//            System.out.println("========================");
//            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
//                if (action == GLFW.GLFW_PRESS) {
//                    dragging = true;
//                    System.out.println("========================");
//
//                    double[] xpos = new double[1];
//                    double[] ypos = new double[1];
//                    GLFW.glfwGetCursorPos(win, xpos, ypos);
//
//                    // 记录鼠标按下时窗口相对位置
//                    dragOffsetX = xpos[0];
//                    dragOffsetY = ypos[0];
//                } else if (action == GLFW.GLFW_RELEASE) {
//                    dragging = false;
//                }
//            }
//        });

    }
}
