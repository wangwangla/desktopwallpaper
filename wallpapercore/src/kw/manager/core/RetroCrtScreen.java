package kw.manager.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.wallper.screen.BasePetGame;
import com.wallper.screen.BasePetScreen;
import kw.manager.core.event.EventListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** All interface drawing happens inside the FBO; the final pass processes it as one image. */
public final class RetroCrtScreen extends BasePetScreen {
    private static final float W = 1200, H = 800;
    private static final Color BG = Color.valueOf("111610");
    private static final Color PANEL = Color.valueOf("191F16");
    private static final Color LINE = Color.valueOf("424734");
    private static final Color AMBER = Color.valueOf("F5BB68");
    private static final Color PAPER = Color.valueOf("E2DFC0");
    private static final Color MUTED = Color.valueOf("92977B");
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("EEEE / dd MMM yyyy", java.util.Locale.ENGLISH);
    private InputAdapter controls;
    private InputMultiplexer input;
    private boolean released;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private ShaderProgram crt;
    private FrameBuffer frameBuffer;
    private TextureRegion frame;
    private final Matrix4 projection = new Matrix4().setToOrtho2D(0, 0, W, H);
    private boolean enabled = true, paused;
    private float strength = 0.7f, time;
    private int section;

    public RetroCrtScreen(BasePetGame game) { super(game); }

    @Override public void initView() {
        super.initView();
        if (batch != null) {
            attachInput();
            return;
        }
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = new BitmapFont(Gdx.files.internal("retro/retro-mono.fnt"));
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        crt = new ShaderProgram(Gdx.files.internal("shaders/retro-crt.vert"), Gdx.files.internal("shaders/retro-crt.frag"));
        if (!crt.isCompiled()) throw new IllegalStateException("CRT shader: " + crt.getLog());
        batch.setProjectionMatrix(projection);
        shapes.setProjectionMatrix(projection);
        controls = new InputAdapter() {
            @Override public boolean keyDown(int key) { return handleKey(key); }
            @Override public boolean touchDown(int x, int y, int pointer, int button) {
                if (button != Input.Buttons.LEFT) return false;
                // Match the post-process UV distortion when hit-testing the rendered page.
                float px = 2f * x / Gdx.graphics.getWidth() - 1f;
                float py = 1f - 2f * y / Gdx.graphics.getHeight();
                float s = enabled ? strength : 0;
                px *= 1f - 0.014f * s; py *= 1f - 0.014f * s;
                float ox = px, oy = py;
                px += ox * oy * oy * 0.055f * s;
                py += oy * ox * ox * 0.075f * s;
                float ux = (px + 1f) * W / 2f, uy = (py + 1f) * H / 2f;
                if (ux >= 52 && ux <= 254) {
                    for (int i = 0; i < 3; i++) if (uy > 504 - i * 58 && uy < 552 - i * 58) { section = i; return true; }
                }
                if (uy >= 54 && uy <= 96) {
                    if (ux >= 54 && ux < 350) enabled = !enabled;
                    else if (ux >= 370 && ux < 580) paused = !paused;
                    else if (ux >= 674 && ux <= 1092) strength = Math.max(0, Math.min(1, (ux - 674) / 418));
                    return true;
                }
                return false;
            }
        };
        // Like MainScreen, populate the Stage through initView/addActor.
        addActor(new Actor() {
            @Override public void draw(Batch stageBatch, float parentAlpha) {
                stageBatch.end();
                try {
                    drawPage();
                } finally {
                    stageBatch.begin();
                }
            }
        });
        attachInput();
    }

    private void attachInput() {
        input = new InputMultiplexer(controls, stage);
        Gdx.input.setInputProcessor(input);
        // WindowGame.setMousePosition receives window-local clicks from MouseHook.
        EventListener.getInstance().addEvent("mousePosisiton", (x, y) ->
                controls.touchDown((int) x, (int) y, 0, Input.Buttons.LEFT));
    }

    private boolean handleKey(int key) {
        switch (key) {
            case Input.Keys.SPACE: enabled = !enabled; break;
            case Input.Keys.P: paused = !paused; break;
            case Input.Keys.LEFT: strength = Math.max(0, strength - 0.1f); break;
            case Input.Keys.RIGHT: strength = Math.min(1, strength + 0.1f); break;
            case Input.Keys.NUM_1: section = 0; break;
            case Input.Keys.NUM_2: section = 1; break;
            case Input.Keys.NUM_3: section = 2; break;
            case Input.Keys.R: strength = 0.7f; enabled = true; paused = false; section = 0; break;
            default: return false;
        }
        return true;
    }

    @Override public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        if (frameBuffer != null) frameBuffer.dispose();
        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888,
                Math.max(1, Gdx.graphics.getBackBufferWidth()), Math.max(1, Gdx.graphics.getBackBufferHeight()), false);
        frameBuffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        frame = new TextureRegion(frameBuffer.getColorBufferTexture());
        frame.flip(false, true);
    }

    @Override public void render(float delta) {
        if (frameBuffer == null) return;
        if (!paused) time += Math.min(delta, 0.05f);
        frameBuffer.begin();
        Gdx.gl.glClearColor(BG.r, BG.g, BG.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        try {
            super.render(delta);
        } finally {
            frameBuffer.end();
        }
        Gdx.gl.glClearColor(0.025f, 0.03f, 0.02f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setShader(crt);
        batch.begin();
        crt.setUniformf("u_resolution", frameBuffer.getWidth(), frameBuffer.getHeight());
        crt.setUniformf("u_time", time);
        crt.setUniformf("u_strength", enabled ? strength : 0);
        batch.setColor(Color.WHITE);
        batch.draw(frame, 0, 0, W, H);
        batch.end();
        batch.setShader(null);
    }

    private void drawPage() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        rect(30, 30, 1140, 740, LINE); rect(32, 32, 1136, 736, BG);
        rect(52, 668, 1096, 1, LINE);
        rect(276, 132, 1, 510, LINE);
        rect(52, 122, 1096, 1, LINE);
        rect(52, 504 - section * 58, 202, 48, AMBER);
        rect(310, 350, 808, 250, PANEL);
        rect(310, 334, 808, 1, LINE);
        rect(52, 54, 298, 42, PANEL); rect(370, 54, 210, 42, PANEL);
        rect(674, 69, 418, 5, LINE); rect(674, 69, 418 * strength, 5, AMBER);
        rect(670 + 418 * strength, 62, 8, 19, AMBER);
        for (int i = 0; i < 12; i++) rect(52 + i * 15, 246, 8, 12 + (i % 4) * 6, i < 9 ? AMBER : LINE);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(LINE);
        for (int x = 336; x < 1100; x += 38) shapes.line(x, 372, x, 505);
        for (int y = 372; y <= 505; y += 22) shapes.line(336, y, 1092, y);
        shapes.setColor(AMBER);
        for (int x = 336; x < 1092; x += 3) shapes.line(x, wave(x), x + 3, wave(x + 3));
        shapes.end();

        batch.begin();
        text("A / H", 54, 736, 2.5f, AMBER);
        text("AFTERHOURS", 208, 739, 2.2f, PAPER);
        text("PERSONAL WORKSTATION   /   EST. 1984", 210, 705, 1.05f, MUTED);
        text("LOCAL SIGNAL", 946, 733, 1.2f, AMBER);
        text("NO NETWORK REQUIRED", 946, 707, 0.9f, MUTED);
        text("DIRECTORY", 54, 623, 1, MUTED);
        String[] nav = {"01  OVERVIEW", "02  SIGNAL LAB", "03  FIELD NOTES"};
        for (int i = 0; i < 3; i++) text(nav[i], 67, 535 - i * 58, 1.15f, i == section ? BG : PAPER);
        text("STAY A LITTLE", 54, 343, 1.25f, PAPER);
        text("OFFLINE.", 54, 315, 1.8f, AMBER);
        text("ANALOG / DIGITAL", 54, 224, 1, MUTED);
        text("REV. 01     60 HZ", 54, 196, 1, MUTED);
        text("SYSTEM READY _", 54, 159, 1, AMBER);
        text(section == 0 ? "A QUIETER KIND OF DESKTOP." : section == 1 ? "TUNE INTO THE PRESENT." : "THINGS WORTH KEEPING.", 310, 634, 1.55f, PAPER);
        text(section == 1 ? "OSCILLOSCOPE / SYNTHETIC SIGNAL" : "LOCAL TIME / LIVE TRANSMISSION", 334, 577, 1, MUTED);
        if (section == 2) {
            text("MAKE ROOM FOR A LITTLE LESS NOISE.", 334, 543, 1.9f, AMBER);
        } else {
            text(LocalDateTime.now().format(CLOCK), 333, 548, 3.7f, AMBER);
            text(LocalDateTime.now().format(DATE).toUpperCase(java.util.Locale.ENGLISH), 730, 540, 1.05f, PAPER);
            text(paused ? "SIGNAL HELD" : "CARRIER STABLE  /  98.4 FM", 730, 514, 1, MUTED);
        }
        text("CHANNEL " + (section + 1) + "   /   " + (paused ? "PAUSED" : "RECEIVING"), 334, 395, 0.95f, PAPER);
        text("SESSION LOG", 310, 307, 1, MUTED);
        String[] lines = section == 0 ? new String[]{"[ OK ]  Clock synchronized to local time.", "[ OK ]  Phosphor display warmed up.", "[ -- ]  The rest of the world can wait."}
                : section == 1 ? new String[]{"[ 01 ]  Live composite waveform / demo signal.", "[ 02 ]  Press P to hold the transmission.", "[ 03 ]  Left / right arrows adjust CRT strength."}
                : new String[]{"[ 01 ]  Finish one thing before starting another.", "[ 02 ]  Leave a little space in the day.", "[ 03 ]  Good ideas do not need notifications."};
        for (int i = 0; i < lines.length; i++) text(lines[i], 310, 270 - i * 33, 1.25f, i == 2 ? AMBER : PAPER);
        text("guest@afterhours:~$", 310, 157, 1.2f, AMBER);
        if ((int)(time * 2) % 2 == 0) text("_", 530, 157, 1.2f, AMBER);
        text("[SPACE] CRT " + (enabled ? "ON" : "OFF / ORIGINAL"), 68, 82, 1.1f, enabled ? AMBER : PAPER);
        text("[P] " + (paused ? "RESUME SIGNAL" : "HOLD SIGNAL"), 384, 82, 1.05f, PAPER);
        text("INTENSITY", 674, 109, 1f, MUTED);
        text(Math.round(strength * 100) + "%", 1098, 82, 1, AMBER);
        batch.end();
    }

    private float wave(float x) {
        float t = (x - 336) / 756f;
        return 440 + (float)(Math.sin(t * 32 + time * 2.1) * Math.sin(t * 7 + time * 0.4) * 29
                + Math.sin(t * 95 - time * 1.3) * (section == 1 ? 13 : 5));
    }
    private void rect(float x, float y, float w, float h, Color c) { shapes.setColor(c); shapes.rect(x, y, w, h); }
    private void text(String value, float x, float y, float scale, Color c) {
        font.getData().setScale(scale); font.setColor(c); font.draw(batch, value, x, y);
    }

    public boolean isCrtEnabled() { return enabled; }
    public boolean isSignalPaused() { return paused; }
    public float getCrtStrength() { return strength; }
    public int getSection() { return section; }

    @Override public void hide() {
        EventListener.getInstance().removeEvent("mousePosisiton");
        if (Gdx.input.getInputProcessor() == input) Gdx.input.setInputProcessor(null);
        super.hide();
    }

    @Override public void dispose() {
        if (released) return;
        released = true;
        hide();
        super.dispose();
        if (frameBuffer != null) frameBuffer.dispose();
        if (crt != null) crt.dispose();
        if (font != null) font.dispose();
        if (shapes != null) shapes.dispose();
        if (batch != null) batch.dispose();
    }
}
