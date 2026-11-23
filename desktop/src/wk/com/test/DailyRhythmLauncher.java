package wk.com.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.wallper.listener.WindowListener;
import kw.manager.core.WindowGame;
import kw.manager.core.DailyRhythmScreen;

/** Windowed preview that never attaches itself to the Windows desktop. */
public final class DailyRhythmLauncher {
    public static void main(String[] args) {
        boolean smoke = args.length > 0 && "--smoke-test".equals(args[0]);
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("A Day in Lines - preview");
        config.setWindowedMode(1280, 720);
        config.setResizable(true);
        config.useVsync(true);
        WindowListener listener = new WindowListener() {
            public void windowForward() { }
            public void moveWindowPosition(float x, float y) { }
            public void setWallpaper() { }
        };
        smoke = false;
        new Lwjgl3Application().init(smoke ? new SmokeGame(listener) : new WindowGame(listener), config);
    }

    private static final class SmokeGame extends WindowGame {
        private int frames;
        SmokeGame(WindowListener listener) { super(listener); }
        @Override public void render() {
            super.render();
            if (!(getScreen() instanceof DailyRhythmScreen)) return;
            DailyRhythmScreen screen = (DailyRhythmScreen)getScreen();
            frames++;
            if (frames >= 3 && frames <= 18 && frames % 3 == 0) {
                int index = frames / 3 - 1;
                screen.setPreviewPhase(DailyRhythmScreen.Phase.values()[index]);
                // Render once with the newly selected scene before reading pixels.
                super.render();
                Pixmap image = Pixmap.createFromFrameBuffer(0, 0,
                        Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
                PixmapIO.writePNG(Gdx.files.local("../work/daily-" + (index + 1) + ".png"), image, -1, true);
                image.dispose();
                if (index == 5) {
                    if (DailyRhythmScreen.phaseAt(7, 0) != DailyRhythmScreen.Phase.TO_WORK
                            || DailyRhythmScreen.phaseAt(9, 0) != DailyRhythmScreen.Phase.WORK
                            || DailyRhythmScreen.phaseAt(18, 0) != DailyRhythmScreen.Phase.TO_HOME
                            || DailyRhythmScreen.phaseAt(22, 0) != DailyRhythmScreen.Phase.SLEEP)
                        throw new IllegalStateException("Schedule boundary check failed");
                    Gdx.app.log("DAILY", "SMOKE PASS: six scenes and schedule boundaries");
                    Gdx.app.exit();
                }
            }
        }
    }
}
