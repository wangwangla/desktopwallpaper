package kw.manager.core.seasons;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.kw.gdx.screen.BaseScreen;

import kw.manager.core.city.*;

import java.awt.image.*;
import java.nio.ByteOrder;

/**
 * All seasonal drawing, animation, weather and settings live in core.
 */
public final class SeasonsScreen extends BaseScreen {
    private volatile SeasonSettings settings;
    public final WeatherService weather;
    private final SeasonArtwork artwork = new SeasonArtwork();
    private final SpriteBatch batch = new SpriteBatch();
    private final Pixmap pixels = new Pixmap(SeasonArtwork.W, SeasonArtwork.H, Pixmap.Format.RGBA8888);
    private final Texture texture;
    private final SeasonsGame seasonsGame;
    private double elapsed;

    public SeasonsScreen(SeasonsGame game) {
        super(game);
        seasonsGame = game;
        settings = game.initialSettings.copy();
        weather = new WeatherService(() -> settings, game.interactive);
        pixels.getPixels().order(ByteOrder.BIG_ENDIAN);
        texture = new Texture(pixels);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    public SeasonSettings settings() {
        return settings.copy();
    }

    public void configure(SeasonSettings next) {
        SeasonSettings before = settings;
        settings = next.copy();
        settings.save();
        if (before.latitude != next.latitude || before.longitude != next.longitude || !before.timezone.equals(next.timezone))
            weather.changingLocation(settings);
    }

    public void preview(int term, int minutes, String mode, float temperature) {
        SeasonSettings s = settings();
        s.previewTerm = term;
        s.previewMinutes = minutes;
        s.weatherMode = mode;
        settings = s;
        WeatherData data = new WeatherData();
        data.temperature = temperature;
        weather.data = data;
        elapsed = 5;
    }

    @Override
    public void render(float delta) {
        elapsed += Math.min(delta, .15f);
        BufferedImage image = artwork.frame(settings, weather.data.preview(settings.weatherMode), weather.status, elapsed);
        int[] rgb = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        java.nio.IntBuffer buffer = pixels.getPixels().asIntBuffer();
        for (int pixel : rgb) buffer.put((pixel << 8) | 255);
        texture.draw(pixels, 0, 0);
        batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.begin();
        float scale = Math.max(Gdx.graphics.getWidth() / (float) SeasonArtwork.W, Gdx.graphics.getHeight() / (float) SeasonArtwork.H);
        float w = SeasonArtwork.W * scale, h = SeasonArtwork.H * scale;
        batch.draw(texture, (Gdx.graphics.getWidth() - w) / 2, (Gdx.graphics.getHeight() - h) / 2, w, h);
        batch.end();
        if (seasonsGame.interactive) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                if (seasonsGame.platform().isWallpaper()) seasonsGame.platform().preview();
                else seasonsGame.platform().quit();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) seasonsGame.platform().showControls();
        }
    }

    @Override
    public void dispose() {
        weather.close();
        texture.dispose();
        pixels.dispose();
        batch.dispose();
        super.dispose();
    }
}
