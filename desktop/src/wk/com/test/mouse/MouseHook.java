package wk.com.test.mouse;

import static org.lwjgl.system.windows.User32.WM_LBUTTONDOWN;

import com.badlogic.gdx.Gdx;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import kw.manager.core.WindowGame;

public class MouseHook {
    private static WinUser.HHOOK hHook;

    private static final WinUser.LowLevelMouseProc mouseHook = new WinUser.LowLevelMouseProc() {
        @Override
        public WinDef.LRESULT callback(int nCode, WinDef.WPARAM wParam, WinUser.MSLLHOOKSTRUCT info) {
            if (nCode >= 0) {
                int msg = wParam.intValue();
                int x = info.pt.x;
                int y = info.pt.y;
                if (msg == WM_LBUTTONDOWN){
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            onMouseLeftDown(x,y);
                        }
                    });
                }
//                switch (msg) {
//                    case WM_LBUTTONDOWN:
//                        onMouseLeftDown(x, y);
//                        break;
//                    case WM_LBUTTONUP:
//                        onMouseLeftUp(x, y);
//                        break;
//                    case WM_RBUTTONDOWN:
//                        onMouseRightDown(x, y);
//                        break;
//                }
            }
            return User32.INSTANCE.CallNextHookEx(hHook, nCode, wParam, new WinDef.LPARAM());
        }
    };

    private static WindowGame windowGame;
    public static void installHook(WindowGame _windowGame) {
        windowGame = _windowGame;
        hHook = User32.INSTANCE.SetWindowsHookEx(
                WinUser.WH_MOUSE_LL,
                mouseHook,
                Kernel32.INSTANCE.GetModuleHandle(null),
                0
        );

        if (hHook == null) {
            throw new RuntimeException("Failed to install mouse hook");
        }

        new Thread(() -> {
            WinUser.MSG msg = new WinUser.MSG();
            while (User32.INSTANCE.GetMessage(msg, null, 0, 0) != 0) {
                User32.INSTANCE.TranslateMessage(msg);
                User32.INSTANCE.DispatchMessage(msg);
            }
        }, "MouseHookThread").start();
    }

    private static void onMouseLeftDown(int x, int y) {
        System.out.println("LeftDown: " + x + ", " + y);
        windowGame.setMousePosition(x,y);
    }

    private static void onMouseLeftUp(int x, int y) {
        System.out.println("LeftUp: " + x + ", " + y);
    }

    private static void onMouseMove(int x, int y) {
//         System.out.println("Move: " + x + ", " + y);
    }

    private static void onMouseRightDown(int x, int y) {
        System.out.println("RightDown: " + x + ", " + y);
    }
}
