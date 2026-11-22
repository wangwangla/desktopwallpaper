package wk.com.test;

import com.badlogic.gdx.Gdx;
import com.sun.jna.*;
import com.sun.jna.platform.win32.*;
import com.sun.jna.win32.W32APIOptions;
import kw.manager.core.muyu.MuyuScreen;
import org.lwjgl.glfw.GLFWNativeWin32;

/** Reads only left-button transitions on the desktop; does not intercept input. */
final class MuyuDesktopMouse {
    interface DesktopApi extends Library { DesktopApi INSTANCE=Native.load("user32",DesktopApi.class,W32APIOptions.DEFAULT_OPTIONS); WinDef.HWND WindowFromPoint(WinDef.POINT.ByValue point); }
    private boolean down;
    void poll(MuyuScreen scene,long glfwWindow,boolean attached){
        boolean pressed=(User32.INSTANCE.GetAsyncKeyState(1)&0x8000)!=0;
        boolean edge=pressed&&!down;down=pressed;if(!edge||!attached||scene==null)return;
        WinDef.POINT point=new WinDef.POINT();if(!User32.INSTANCE.GetCursorPos(point))return;
        WinDef.POINT.ByValue value=new WinDef.POINT.ByValue();value.x=point.x;value.y=point.y;
        WinDef.HWND target=DesktopApi.INSTANCE.WindowFromPoint(value);char[] name=new char[128];User32.INSTANCE.GetClassName(target,name,name.length);String type=Native.toString(name);
        WinDef.RECT rect=new WinDef.RECT();WinDef.HWND window=new WinDef.HWND(Pointer.createConstant(GLFWNativeWin32.glfwGetWin32Window(glfwWindow)));
        boolean ownWindow=target!=null&&window.getPointer().equals(target.getPointer());
        if(!(ownWindow||"SysListView32".equals(type)||"SHELLDLL_DefView".equals(type)||"WorkerW".equals(type)||"Progman".equals(type)))return;
        if(!User32.INSTANCE.GetWindowRect(window,rect)||rect.right<=rect.left||rect.bottom<=rect.top)return;
        int x=(int)((point.x-rect.left)*(double)Gdx.graphics.getWidth()/(rect.right-rect.left));
        int y=(int)((point.y-rect.top)*(double)Gdx.graphics.getHeight()/(rect.bottom-rect.top));scene.click(x,y);
    }
}
