package wk.com.test;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.wallper.listener.WindowListener;
import kw.manager.core.WindowGame;
import kw.manager.core.RetroCrtScreen;

/** Independent preview: does not attach to or replace the Windows wallpaper. */
public final class RetroCrtLauncher {
    public static void main(String[] args) {
        boolean smoke = args.length > 0 && "--smoke-test".equals(args[0]);
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("AFTERHOURS / Analog workstation");
        config.setWindowedMode(1200, 800);
        config.setResizable(true);
        config.useVsync(true);
        WindowListener previewListener = new WindowListener() {
            public void windowForward() { }
            public void moveWindowPosition(float x, float y) { }
            public void setWallpaper() { }
        };
        WindowGame game = smoke ? new SmokeGame(previewListener) : new WindowGame(previewListener);
        new Lwjgl3Application().init(game, config);
    }

    /** Exercise the same WindowGame/Screen path, without changing the Windows desktop. */
    private static final class SmokeGame extends WindowGame {
        private int frames;
        SmokeGame(WindowListener listener) { super(listener); }
        @Override public void render() {
            super.render();
            if (!(getScreen() instanceof RetroCrtScreen)) return;
            RetroCrtScreen screen = (RetroCrtScreen) getScreen();
            frames++;
            if (frames == 3 || frames == 5) {
                Pixmap image = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
                PixmapIO.writePNG(Gdx.files.local("../work/retro-crt-" + (screen.isCrtEnabled() ? "on" : "off") + ".png"), image, -1, true);
                image.dispose();
            }
            if (frames == 3) Gdx.input.getInputProcessor().keyDown(Input.Keys.SPACE);
            if (frames == 5) {
                // Exercise the real desktop-hook event route, not just keyboard controls.
                setMousePosition(150, 330);
                Gdx.input.getInputProcessor().keyDown(Input.Keys.P);
                Gdx.input.getInputProcessor().keyDown(Input.Keys.RIGHT);
                if (screen.isCrtEnabled() || !screen.isSignalPaused() || screen.getSection() != 1 || screen.getCrtStrength() < 0.79f)
                    throw new IllegalStateException("Screen input check failed");
                Gdx.input.getInputProcessor().keyDown(Input.Keys.R);
                Gdx.graphics.setWindowedMode(960, 640);
            }
            if (frames == 10) {
                if (Gdx.gl.glGetError() != GL20.GL_NO_ERROR) throw new IllegalStateException("OpenGL error");
                // Check hide/show without duplicating actors or leaving stale input listeners.
                screen.hide(); screen.show();
                if (screen.getStage().getActors().size != 1) throw new IllegalStateException("Duplicate page actors");
                setMousePosition(120, 264);
                if (screen.getSection() != 1) throw new IllegalStateException("Resize/event reattachment failed");
                Gdx.app.log("CRT", "SMOKE PASS: WindowGame, BasePetScreen, Stage, shader, desktop click, resize, hide/show");
                Gdx.app.exit();
            }
        }
    }
}
