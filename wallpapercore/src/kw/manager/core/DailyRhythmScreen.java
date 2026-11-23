package kw.manager.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.wallper.screen.BasePetGame;
import com.wallper.screen.BasePetScreen;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** A quiet, hand-drawn day that changes with local time. */
public final class DailyRhythmScreen extends BasePetScreen {
    public enum Phase { MORNING, TO_WORK, WORK, TO_HOME, EVENING, SLEEP }

    private static final float W = 1920f, H = 1080f;
    private static final Color INK = Color.valueOf("26313A");
    private static final Color PAPER = Color.valueOf("F4EBDD");
    private static final Color NIGHT = Color.valueOf("17233A");
    private static final Color BLUE = Color.valueOf("A9C9D8");
    private static final Color SUN = Color.valueOf("F0B86E");
    private static final Color GREEN = Color.valueOf("91AB8B");
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE, MMM d", java.util.Locale.ENGLISH);

    private final Matrix4 projection = new Matrix4().setToOrtho2D(0, 0, W, H);
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;
    private InputAdapter input;
    private float elapsed;
    private Phase preview;
    private boolean released;

    public DailyRhythmScreen(BasePetGame game) { super(game); }

    @Override public void initView() {
        super.initView();
        if (shapes == null) {
            shapes = new ShapeRenderer();
            batch = new SpriteBatch();
            font = new BitmapFont(Gdx.files.internal("retro/retro-mono.fnt"));
            shapes.setProjectionMatrix(projection);
            batch.setProjectionMatrix(projection);
            input = new InputAdapter() {
                @Override public boolean keyDown(int keycode) {
                    if (keycode == Input.Keys.NUM_0) { preview = null; return true; }
                    if (keycode >= Input.Keys.NUM_1 && keycode <= Input.Keys.NUM_6) {
                        preview = Phase.values()[keycode - Input.Keys.NUM_1];
                        return true;
                    }
                    return false;
                }
            };
        }
        Gdx.input.setInputProcessor(input);
    }

    public static Phase phaseAt(int hour, int minute) {
        int now = hour * 60 + minute;
        if (now >= 7 * 60 && now < 9 * 60) return Phase.TO_WORK;
        if (now >= 9 * 60 && now < 18 * 60) return Phase.WORK;
        if (now >= 18 * 60 && now < 20 * 60) return Phase.TO_HOME;
        if (now >= 20 * 60 && now < 22 * 60) return Phase.EVENING;
        if (now >= 22 * 60 || now < 5 * 60) return Phase.SLEEP;
        return Phase.MORNING;
    }

    public Phase currentPhase() {
        LocalDateTime now = LocalDateTime.now();
        return preview == null ? phaseAt(now.getHour(), now.getMinute()) : preview;
    }

    public void setPreviewPhase(Phase phase) { preview = phase; }

    @Override public void render(float delta) {
        elapsed += Math.min(delta, 0.05f);
        Phase phase = currentPhase();
        Color background = phase == Phase.SLEEP ? NIGHT : skyFor(phase);
        Gdx.gl.glClearColor(background.r, background.g, background.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        drawScene(phase);
    }

    private Color skyFor(Phase phase) {
        switch (phase) {
            case TO_WORK: return Color.valueOf("E8E0CF");
            case WORK: return Color.valueOf("E6EDF0");
            case TO_HOME: return Color.valueOf("D9B9A4");
            case EVENING: return Color.valueOf("C9CED1");
            default: return PAPER;
        }
    }

    private void drawScene(Phase phase) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (phase == Phase.SLEEP) drawSleep();
        else if (phase == Phase.WORK) drawWork();
        else if (phase == Phase.TO_WORK || phase == Phase.TO_HOME) drawWalk(phase == Phase.TO_HOME);
        else drawHome(phase == Phase.EVENING);
        shapes.end();
        drawHeader(phase);
    }

    private void drawHeader(Phase phase) {
        LocalDateTime now = LocalDateTime.now();
        Color primary = phase == Phase.SLEEP ? Color.valueOf("E7E0CE") : INK;
        batch.begin();
        font.getData().setScale(2.2f); font.setColor(primary);
        font.draw(batch, now.format(CLOCK), 112, 968);
        font.getData().setScale(1.15f); font.setColor(primary.cpy().mul(1, 1, 1, .62f));
        font.draw(batch, now.format(DAY).toUpperCase(java.util.Locale.ENGLISH), 114, 918);
        font.getData().setScale(1.25f); font.setColor(primary);
        font.draw(batch, label(phase), 1560, 958);
        if (phase == Phase.SLEEP) {
            font.setColor(Color.valueOf("E9DDAA"));
            font.getData().setScale(1.2f); font.draw(batch, "z", 1340, 650 + (elapsed * 14) % 28);
            font.getData().setScale(1.65f); font.draw(batch, "z", 1390, 710 + (elapsed * 11) % 35);
            font.getData().setScale(2.1f); font.draw(batch, "Z", 1450, 785 + (elapsed * 8) % 42);
        }
        batch.end();
    }

    private String label(Phase phase) {
        switch (phase) {
            case MORNING: return "A SLOW MORNING";
            case TO_WORK: return "ON THE WAY";
            case WORK: return "FOCUS TIME";
            case TO_HOME: return "HEADING HOME";
            case EVENING: return "BACK HOME";
            default: return "GOOD NIGHT";
        }
    }

    private void drawWalk(boolean homeward) {
        Color skyAccent = homeward ? Color.valueOf("D8846D") : SUN;
        circle(homeward ? 1580 : 330, 790, 72, skyAccent);
        // distant city, trees and long road
        rect(0, 0, W, 285, Color.valueOf(homeward ? "AA8274" : "BCC8B1"));
        for (int i = 0; i < 10; i++) {
            float x = 55 + i * 205;
            float h = 125 + (i % 4) * 42;
            rect(x, 285, 130, h, Color.valueOf(homeward ? "8F7772" : "B4BCAF"));
            for (int w = 0; w < 3; w++) rect(x + 18 + w * 35, 315 + (w % 2) * 45, 14, 22, skyAccent);
        }
        rect(0, 0, W, 195, Color.valueOf("8C9290"));
        rect(0, 115, W, 7, Color.valueOf("D9D0BB"));
        for (int i = 0; i < 9; i++) rect(i * 250 + 40, 151, 140, 6, Color.valueOf("D9D0BB"));
        float travel = (elapsed * 92f) % (W + 320) - 160;
        float x = homeward ? W - travel : travel;
        drawPerson(x, 300, true, homeward ? -1 : 1);
        // drifting clouds make the wallpaper feel alive without being busy
        for (int i = 0; i < 3; i++) {
            float cx = (i * 680 + elapsed * (9 + i * 2)) % 2200 - 120;
            cloud(cx, 720 + i * 52, Color.valueOf("F5F0E5"));
        }
    }

    private void drawWork() {
        rect(0, 0, W, 230, Color.valueOf("CAD5D7"));
        rect(240, 230, 1440, 18, INK);
        rect(480, 248, 960, 310, Color.valueOf("D9CBB6"));
        rect(520, 558, 370, 230, INK);
        rect(535, 574, 340, 196, Color.valueOf("9FBBC7"));
        rect(1040, 558, 370, 230, INK);
        rect(1055, 574, 340, 196, Color.valueOf("AFC4B0"));
        // monitor and small moving cursor
        rect(790, 470, 340, 205, INK);
        rect(806, 486, 308, 170, Color.valueOf("F2E8D5"));
        rect(940, 425, 40, 45, INK); rect(870, 414, 180, 14, INK);
        for (int i = 0; i < 5; i++) rect(838, 620 - i * 25, 105 + (i % 3) * 55, 6, i == 2 ? SUN : BLUE);
        if (((int)(elapsed * 2)) % 2 == 0) rect(1005, 520, 8, 19, INK);
        drawPerson(660, 310, false, 1);
        // plant
        rect(1370, 350, 75, 92, Color.valueOf("B77E62"));
        ellipse(1375, 425, 50, 105, GREEN); ellipse(1420, 430, 48, 95, GREEN);
    }

    private void drawHome(boolean evening) {
        rect(250, 200, 1420, 520, Color.valueOf(evening ? "DDD5CA" : "E7DDCA"));
        // window
        rect(1090, 420, 390, 250, INK);
        rect(1105, 435, 360, 220, evening ? NIGHT : BLUE);
        rect(1280, 435, 12, 220, INK); rect(1105, 540, 360, 10, INK);
        if (evening) { circle(1385, 590, 28, Color.valueOf("EAD9A7")); star(1180, 605); star(1330, 500); }
        // sofa and lamp
        rect(440, 265, 610, 175, Color.valueOf("A8B2A0"));
        rect(405, 280, 80, 200, Color.valueOf("8C9C89")); rect(1010, 280, 80, 200, Color.valueOf("8C9C89"));
        rect(1510, 260, 12, 300, INK); circle(1516, 575, 72, SUN);
        drawPerson(750, 365 + (float)Math.sin(elapsed * 1.2f) * 3, false, 1);
        rect(0, 0, W, 200, Color.valueOf("C6B9A3"));
    }

    private void drawSleep() {
        // moon, stars, room silhouette
        circle(1515, 790, 78, Color.valueOf("E9DDAA"));
        circle(1550, 818, 76, NIGHT);
        for (int i = 0; i < 18; i++) star(90 + (i * 103) % 1710, 660 + (i * 61) % 300);
        rect(0, 0, W, 190, Color.valueOf("10192A"));
        rect(310, 190, 1300, 285, Color.valueOf("26324A"));
        rect(310, 475, 50, 120, Color.valueOf("303B52"));
        rect(360, 420, 1250, 165, Color.valueOf("52627A"));
        rect(1180, 500, 340, 85, Color.valueOf("D9D5C5"));
        circle(1265, 555, 48, Color.valueOf("D6A27D"));
        // blanket curve approximation and breathing motion
        float breathe = (float)Math.sin(elapsed * 1.7f) * 8;
        ellipse(690, 455, 760, 155 + breathe, Color.valueOf("7888A0"));
        rect(360, 420, 1250, 92, Color.valueOf("7888A0"));
    }

    private void drawPerson(float x, float y, boolean walking, int direction) {
        float step = walking ? (float)Math.sin(elapsed * 7f) : 0;
        Color skin = Color.valueOf("D6A27D");
        circle(x, y + 235, 43, skin);
        // hair and body
        circle(x - 7 * direction, y + 252, 39, INK);
        circle(x + 8 * direction, y + 230, 35, skin);
        rect(x - 48, y + 70, 96, 145, Color.valueOf("718B91"));
        thickLine(x - 25, y + 72, x - 44 + step * 25, y, 18, INK);
        thickLine(x + 25, y + 72, x + 50 - step * 25, y, 18, INK);
        thickLine(x - 42, y + 185, x - 70 - step * 18, y + 105, 15, skin);
        thickLine(x + 42, y + 185, x + 68 + step * 18, y + 105, 15, skin);
        if (walking) { rect(x - 78 * direction, y + 90, 42, 70, Color.valueOf("B87958")); }
    }

    private void cloud(float x, float y, Color c) {
        circle(x, y, 42, c); circle(x + 50, y + 15, 55, c); circle(x + 112, y, 42, c); rect(x, y - 28, 112, 56, c);
    }
    private void star(float x, float y) { circle(x, y, 3.5f, Color.valueOf("E9DDAA")); }
    private void rect(float x, float y, float w, float h, Color c) { shapes.setColor(c); shapes.rect(x, y, w, h); }
    private void circle(float x, float y, float r, Color c) { shapes.setColor(c); shapes.circle(x, y, r, 36); }
    private void ellipse(float x, float y, float w, float h, Color c) { shapes.setColor(c); shapes.ellipse(x, y, w, h, 48); }
    private void thickLine(float x1, float y1, float x2, float y2, float width, Color c) {
        shapes.setColor(c); shapes.rectLine(x1, y1, x2, y2, width);
    }

    @Override public void hide() {
        if (Gdx.input.getInputProcessor() == input) Gdx.input.setInputProcessor(null);
        super.hide();
    }

    @Override public void dispose() {
        if (released) return;
        released = true;
        hide();
        super.dispose();
        if (font != null) font.dispose();
        if (batch != null) batch.dispose();
        if (shapes != null) shapes.dispose();
    }
}
