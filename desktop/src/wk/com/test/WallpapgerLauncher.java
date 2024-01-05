package wk.com.test;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;

import org.lwjgl.glfw.GLFW;

import kw.manager.core.WindowManagerGame;
import kw.manager.core.listener.ManagerListener;

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
        app.init(new WindowManagerGame(new ManagerListener(){
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
                GLFW.glfwGetWindowPos(windowHandle,windowX,windowY);

//                DynamicUtils.makeWallpaper(windowHandle);




//                double xArr[] =  new double[1];
//                double yArr[] =  new double[1];
//                GLFW.glfwGetCursorPos(windowHandle,xArr,yArr);
//                1090.0-----------1001.0
//                1154.0-----------192.0
//                System.out.println(windowX[0]+"-----------"+windowY[0]);
////                GLFW.glfwSetWindowPos(windowHandle,windowX[0]+(int)x,windowY[0]+(int)y);
////                WinDef.HWND hwnd = User32.INSTANCE.GetDesktopWindow();
////                User32.INSTANCE.SE
////                IntPtr hProgman = DllImports.FindWindow("Progman", "Program Manager");
//                WinDef.HWND hProgman = User32.INSTANCE.FindWindow("Progman", "Program Manager");
////                WinDef.HWND hProgman = User32.INSTANCE.GetDesktopWindow();
//                //发送0x52c消息
//                User32.INSTANCE.SendMessageTimeout(hProgman, 0x52c, new WinDef.WPARAM(0)
//                        , new WinDef.LPARAM(0),0,100,new WinDef.DWORDByReference());
//                WinDef.HWND workerW=null;
//                do
//                {
//                    workerW = User32.INSTANCE.FindWindowEx(null, workerW, "workerW", null);
//                    if (User32.INSTANCE.GetParent(workerW) == hProgman)
//                    {
//                        User32.INSTANCE.ShowWindow(workerW, 0);
//                    }
//                } while (workerW !=null);
//
//                WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, "");
//
//                User32.INSTANCE.SetParent(hwnd, hProgman);
//                DllImports.SetParent(ffplay.MainWindowHandle, hProgman);


//                WinDef.HWND hwnd1 = User32.INSTANCE.FindWindow(null, "windowswallpaper");
//                User32.INSTANCE.ShowWindow(hwnd1,3);
//                //将视频窗口设置为桌面窗口的子窗口
//                User32.INSTANCE.SetParent(hwnd1, hProgman);
//                //枚举窗口找到WorkerW-2并隐藏它
//


//                User32.INSTANCE.FindWindow("Progman",null);

            }
        }), config);
    }
}
