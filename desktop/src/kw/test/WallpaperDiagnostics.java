package kw.test;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;

/** Prints only the shell wallpaper hierarchy, for troubleshooting Windows versions. */
public final class WallpaperDiagnostics {
    private static String name(WinDef.HWND h){char[] s=new char[256];User32.INSTANCE.GetClassName(h,s,s.length);return Native.toString(s);}
    private static long id(WinDef.HWND h){return h==null?0:Pointer.nativeValue(h.getPointer());}
    private static void print(WinDef.HWND h,String prefix){WinDef.RECT r=new WinDef.RECT();User32.INSTANCE.GetWindowRect(h,r);
        System.out.printf("%s%s hwnd=%x parent=%x visible=%s style=%08x ex=%08x rect=%d,%d %dx%d%n",prefix,name(h),id(h),id(User32.INSTANCE.GetParent(h)),User32.INSTANCE.IsWindowVisible(h),User32.INSTANCE.GetWindowLong(h,User32.GWL_STYLE),User32.INSTANCE.GetWindowLong(h,User32.GWL_EXSTYLE),r.left,r.top,r.right-r.left,r.bottom-r.top);}
    public static void main(String[] args){User32.INSTANCE.EnumWindows((h,d)->{String c=name(h);if(("Progman".equals(c)||"WorkerW".equals(c))&&User32.INSTANCE.IsWindowVisible(h)){print(h,"");User32.INSTANCE.EnumChildWindows(h,(child,data)->{print(child,"  ");return true;},null);}return true;},null);}
}
