package wk.com.test;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ScreenUtils;

import kw.manager.core.muyu.MuyuScreen;
import kw.joker.DynamicUtils;
import kw.wallpaper.*;

import org.lwjgl.glfw.GLFW;

@WallpaperDefinition(id="muyu",title="一念木鱼",description="轻敲木鱼，累计功德 · 作息与工作进度",accent=0x9d6b45,
    thumbnail="wallpaper-previews/muyu.png",sceneTitle="一念木鱼 · LibGDX",controllerTitle="一念木鱼 · 壁纸控制器",executables={"Muyu.exe"})
public final class MuyuLauncher {
    private Lwjgl3Application app;
    private MuyuScreen scene;
    private volatile boolean attached;
    private WallpaperSession session;
    private final MuyuDesktopMouse mouse = new MuyuDesktopMouse();
    private Lwjgl3ApplicationConfiguration config;

    public static void main(String[] args) {
        if (WallpaperSession.forwardExisting("muyu",args)) return;
        new MuyuLauncher().run(args);
    }

    private void run(String[] args) {
        String snapshot = null, workPreview = null;
        boolean wall = false, strokePreview = false;
        for (int i = 0; i < args.length; i++) {
            if ("--wallpaper".equals(args[i])) wall = true;
            if ("--snapshot".equals(args[i]) && i + 1 < args.length) snapshot = args[++i];
            if ("--work-preview".equals(args[i]) && i + 1 < args.length) workPreview = args[++i];
            if ("--stroke-snapshots".equals(args[i]) && i + 1 < args.length) {
                snapshot = args[++i];
                strokePreview = true;
            }
        }
        final String output = snapshot, workTime = workPreview;
        final boolean initialWall = wall, animationPreview = strokePreview;
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("一念木鱼 · LibGDX");
        config.setWindowedMode(1200, 800);
        config.setWindowPosition(80, 60);
        config.setResizable(true);
        config.setForegroundFPS(60);
        config.setIdleFPS(60);
        config.setBackBufferConfig(8, 8, 8, 8, 0, 0, 4);
        config.setInitialVisible(output == null);
        config.disableAudio(output != null);
        app = new Lwjgl3Application();
        app.init(new ApplicationAdapter() {
            int frame;

            @Override
            public void create() {
                scene = new MuyuScreen(new MuyuScreen.DesktopActions() {
                    public void wallpaper() {
                        attach();
                    }

                    public void preview() {
                        detach();
                    }

                    public void quit() {
                        if (attached) detach();
                        Gdx.app.exit();
                    }
                }, output == null);
                if (workTime != null)
                    scene.previewWorkTime(java.time.LocalDateTime.parse(workTime));
                if (initialWall) Gdx.app.postRunnable(MuyuLauncher.this::attach);
                if (output == null) try {
                    session=new WallpaperSession("muyu",()->attached?"wallpaper":"preview",Gdx.app::postRunnable,command->{
                        switch(command){case "wallpaper"->attach();case "preview"->detach();case "controls"->scene.showController();case "stop"->{if(attached)detach();Gdx.app.exit();}}
                    });
                } catch (java.io.IOException e) { throw new RuntimeException(e); }
            }

            @Override
            public void resize(int w, int h) {
                if (scene != null) scene.resize(w, h);
            }

            @Override
            public void render() {
                mouse.poll(scene, app.getWindowHandle(), attached);
                scene.render(animationPreview ? .02f : Gdx.graphics.getDeltaTime());
                if (output != null) {
                    frame++;
                    String name = null;
                    if (animationPreview) {
                        if (frame == 12) name = "rest";
                        if (frame == 16) name = "raised";
                        if (frame == 20) name = "contact";
                        if (frame == 29) name = "returned";
                    }
                    if (name != null || (!animationPreview && frame == 12)) {
                        Pixmap image = ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
                        FileHandle file = animationPreview ? new FileHandle(output).child(name + ".png") : new FileHandle(output);
                        PixmapIO.writePNG(file, image, -1, true);
                        image.dispose();
                    }
                    if (animationPreview && frame == 12) scene.hit();
                    if ((animationPreview && frame == 29) || (!animationPreview && frame == 12))
                        Gdx.app.exit();
                }
            }

            @Override
            public void dispose() {
                if (session != null) session.close();
                if (attached) detach();
                if (scene != null) scene.dispose();
            }
        }, config);
        System.exit(0);
    }

    private void attach() {
        if (!attached) {
            DynamicUtils.makeWallpaper(app.getWindowHandle());
            attached = true;
        }
        scene.setWallpaper(true);
    }

    private void detach() {
        if (attached) {
            DynamicUtils.destroyWallpaper(app.getWindowHandle());
            attached = false;
        }
        GLFW.glfwSetWindowSize(app.getWindowHandle(), 1200, 800);
        GLFW.glfwSetWindowPos(app.getWindowHandle(), 80, 60);
        GLFW.glfwShowWindow(app.getWindowHandle());
        scene.setWallpaper(false);
    }
}
