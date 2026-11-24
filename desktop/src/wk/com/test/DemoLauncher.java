package wk.com.test;

import kw.desktop.controls.DemoController;
import kw.desktop.icons.DesktopIconReader;
import kw.manager.core.*;
import kw.wallpaper.*;

import com.badlogic.gdx.Gdx;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;

import org.lwjgl.glfw.GLFWNativeWin32;

import javax.swing.SwingUtilities;

import java.util.List;

@WallpaperDefinition(id = "demo", title = "图标背景 Demo", description = "桌面图标背景 · 删除同步消失，移动缓动跟随", accent = 0x567fa4,
        thumbnail = "wallpaper-previews/demo.png", sceneTitle = "Demo", controllerTitle = "图标背景 Demo · 控制器", executables = {"Demo.exe"})
public class DemoLauncher {
    public static void main(String[] args) throws Exception {
        if (java.util.Arrays.asList(args).contains("--inspect-icons")) {
            try (DesktopIconReader reader = new DesktopIconReader()) {
                var snapshot = reader.read(null);
                System.out.println("Desktop icons: " + snapshot.icons().size() + "; pixels: " + snapshot.width() + "x" + snapshot.height());
            }
            return;
        }
        if (WallpaperSession.forwardExisting("demo", args)) return;
        DesktopLaunchOptions options = new DesktopLaunchOptions(args);
        DesktopWallpaperHost host = new DesktopWallpaperHost("demo", 1200, 800, options.interactive(), options.wallpaper, options.quiet);
        int[] frames = {0}, lastCount = {-1};
        host.run(options.window("Demo", 1200, 800, 60, false, false), DemoGame::new, game -> {
            if (options.interactive()) {
                DesktopIconReader reader = new DesktopIconReader();
                reader.start(new WinDef.HWND(Pointer.createConstant(GLFWNativeWin32.glfwGetWin32Window(host.windowHandle()))), host::isWallpaper);
                host.iconSource(reader::snapshot);
                host.releaseWith(reader::close);
                SwingUtilities.invokeLater(() -> {
                    DemoController controls = new DemoController(host);
                    host.controls(controls::show, controls::show, controls::dispose);
                });
            } else
                host.iconSource(() -> new DesktopIconSnapshot(1200, 800, List.of(new DesktopIconSnapshot.Icon("a", "A", 50, 60, 86, 96), new DesktopIconSnapshot.Icon("b", "B", 50, 210, 86, 96), new DesktopIconSnapshot.Icon("c", "C", 230, 60, 86, 96)), true, "预览"));
        }, game -> {
            int count = ((DemoGame) game).wallpaperScreen().backgroundCount();
            if (options.interactive() && count != lastCount[0]) {
                lastCount[0] = count;
                System.out.println("Demo icon backgrounds: " + count);
            }
            if (options.output != null && ++frames[0] == 12) {
                DesktopLaunchOptions.capture(options.output);
                Gdx.app.exit();
            }
        });
    }
}
