package kw.manager.core.muyu;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.kw.gdx.screen.BaseScreen;

import java.nio.*;
import java.nio.file.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;

import kw.manager.core.WallpaperAssets;
import kw.manager.core.WallpaperPlatform;

public final class MuyuScreen extends BaseScreen {
    private final WallpaperPlatform actions;
    public final MuyuProgress progress;
    private final WallpaperAssets assets = new WallpaperAssets();
    private final Texture background = assets.texture("muyu/background.png"), fish = assets.texture("muyu/fish.png"), stick = assets.texture("muyu/stick.png");
    private final MuyuStroke stroke = new MuyuStroke();
    private final WorkDashboard work;
    private final Label title = assets.label("一 念", 26, 0x4f3d2b), caption = assets.label("累 计 功 德", 16, 0x94826c), hint = assets.label("点击木鱼，或轻按空格", 16, 0x94826c), footer = assets.label("一声木鱼，片刻清静。", 16, 0x94826c), desktopHint = assets.label("轻点木鱼 · 把烦恼放一放", 16, 0x94826c), count = assets.label("0", 88, 0x43362a, true), today = assets.label("", 17, 0x94826c), sessionText = assets.label("", 16, 0x7a6750);
    private long shownTotal = -1, shownToday = -1, shownSession = -1;
    private volatile long session;
    private final Group content = new Group() {
        @Override
        public void act(float delta) {
            float step = ((MuyuGame) game).previewDelta > 0 ? ((MuyuGame) game).previewDelta : delta;
            update(step);
            super.act(step);
        }
    };
    private Group workActors;
    private Image backgroundActor, fishActor, stickActor;
    private Label titleActor, captionActor, countActor, todayActor, hintActor, sessionActor, footerActor;
    private float hitAnimation, automaticElapsed, saveDelay = -1;
    private boolean automatic, wallpaper;
    private Sound sound;
    private long lastHit;
    public volatile String status = "已保存到本机";

    public MuyuScreen(MuyuGame game) {
        super(game);
        this.actions = game.platform();
        boolean interactive = game.interactive;
        work = new WorkDashboard(interactive, assets);
        progress = new MuyuProgress(interactive ? MuyuProgress.defaultFile() : Paths.get(System.getProperty("java.io.tmpdir"), "muyu-preview-unused.properties"));
        if (interactive) createSound();
    }

    public void setWallpaper(boolean value) {
        wallpaper = value;
        status = value ? "桌面壁纸 · 点击木鱼或右侧卡片" : "窗口预览 · 点击木鱼或按空格";
    }

    public void showController() {
        actions.showControls();
    }

    public WorkSettings workSettings() {
        return work.settings();
    }

    public boolean configureWork(WorkSettings value) {
        try {
            work.configure(value);
            status = "作息设置已保存到本机";
            return true;
        } catch (Exception e) {
            status = "作息设置保存失败";
            System.err.println(e);
            return false;
        }
    }

    public void previewWorkTime(java.time.LocalDateTime time) {
        work.preview(time);
    }

    private float woodCenter() {
        return stage.getViewport().getWorldWidth() / 2 - (work.visible() ? 250 : 0);
    }

    private float verticalOffset() {
        return (stage.getViewport().getWorldHeight() - 800) / 2;
    }

    private float cardX() {
        return stage.getViewport().getWorldWidth() / 2 + 90;
    }

    private float cardY() {
        return (stage.getViewport().getWorldHeight() - WorkDashboard.HEIGHT) / 2;
    }


    public void hit() {
        long now = System.nanoTime();
        if (now - lastHit < 75_000_000) return;
        lastHit = now;
        stroke.request();
    }

    private void contact() {
        progress.hit(LocalDate.now());
        session++;
        hitAnimation = .14f;
        Label feedback = assets.label("功德 +1", 28, 0x9d6b45);
        feedback.pack();
        feedback.setPosition(woodCenter() - 185 - feedback.getWidth() / 2, 475 + verticalOffset());
        feedback.addAction(Actions.sequence(Actions.parallel(Actions.moveBy(0, 90, 1), Actions.fadeOut(1)), Actions.removeActor()));
        content.addActor(feedback);
        if (saveDelay < 0) saveDelay = .5f;
        if (progress.sound && sound != null)
            sound.play(.7f, .96f + (float) Math.random() * .08f, 0);
    }

    public boolean automatic() {
        return automatic;
    }

    public void automatic(boolean value) {
        automatic = value;
        automaticElapsed = 0;
    }

    public long session() {
        return session;
    }

    public void sound(boolean value) {
        progress.sound = value;
        saveDelay = .2f;
    }

    public void interval(float value) {
        progress.interval = value;
        saveDelay = .2f;
    }

    private void save() {
        try {
            progress.save();
        } catch (Exception error) {
            status = "记录保存失败，当前计数仍可继续";
            System.err.println(error);
        }
    }

    private void createSound() {
        try {
            Path file = MuyuProgress.defaultFile().resolveSibling("wood.wav");
            Files.createDirectories(file.getParent());
            int rate = 22050, n = rate / 3;
            ByteBuffer wav = ByteBuffer.allocate(44 + n * 2).order(ByteOrder.LITTLE_ENDIAN);
            wav.put("RIFF".getBytes("US-ASCII")).putInt(36 + n * 2).put("WAVEfmt ".getBytes("US-ASCII")).putInt(16).putShort((short) 1).putShort((short) 1).putInt(rate).putInt(rate * 2).putShort((short) 2).putShort((short) 16).put("data".getBytes("US-ASCII")).putInt(n * 2);
            Random random = new Random(9);
            for (int i = 0; i < n; i++) {
                double t = i / (double) rate, attack = Math.min(1, t / .002);
                double s = 0;
                double[] f = {510, 830, 1240, 1870}, amp = {.6, .25, .13, .06}, decay = {.045, .027, .013, .008};
                for (int j = 0; j < f.length; j++)
                    s += amp[j] * Math.sin(2 * Math.PI * f[j] * (t - .025 * t * t)) * Math.exp(-t / decay[j]);
                s += .14 * (random.nextDouble() * 2 - 1) * Math.exp(-t / .003);
                wav.putShort((short) (Math.max(-1, Math.min(1, s * attack)) * 26000));
            }
            Files.write(file, wav.array());
            sound = Gdx.audio.newSound(new FileHandle(file.toFile()));
        } catch (Exception error) {
            status = "音效暂不可用，仍可敲击计数";
            System.err.println(error);
        }
    }

    private void updateLabels() {
        if (shownTotal != progress.total) {
            count.setText(String.format(Locale.CHINA, "%,d", progress.total));
            shownTotal = progress.total;
        }
        if (shownToday != progress.today) {
            today.setText("今日 " + progress.today + " 次 · 慢慢来，不着急");
            shownToday = progress.today;
        }
        if (shownSession != session) {
            sessionText.setText("本次已敲 " + session + " 下");
            shownSession = session;
        }
    }

    private Label addLabel(Label label) {
        content.addActor(label);
        label.pack();
        return label;
    }

    private Image image(Texture texture) {
        Image actor = new Image(texture);
        actor.setTouchable(Touchable.disabled);
        return actor;
    }

    private Image addImage(Texture texture) {
        Image actor = image(texture);
        content.addActor(actor);
        return actor;
    }

    private void listen(Actor actor, Runnable action) {
        actor.setTouchable(Touchable.enabled);
        actor.addListener(new ClickListener(Input.Buttons.LEFT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                action.run();
            }
        });
    }

    @Override
    public void initView() {
        if (backgroundActor != null) return;
        updateLabels();
        backgroundActor = addImage(background);
        titleActor = addLabel(title);
        captionActor = addLabel(caption);
        countActor = addLabel(count);
        todayActor = addLabel(today);
        fishActor = addImage(fish);
        fishActor.setName("muyu");
        listen(fishActor, this::hit);
        stickActor = addImage(stick);
        stickActor.setOrigin(27, 30);
        hintActor = addLabel(hint);
        sessionActor = addLabel(sessionText);
        footerActor = addLabel(footer);
        workActors = work.view(actions::showWorkControls, message -> status = message);
        content.addActor(workActors);
        stage.addActor(content);
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int key) {
                if (key == Input.Keys.SPACE && !wallpaper) {
                    hit();
                    return true;
                }
                if (key == Input.Keys.F1) {
                    actions.showControls();
                    return true;
                }
                return false;
            }
        });
        layoutActors();
    }

    private void centered(Label actor, float y) {
        if (actor == countActor) {
            actor.setFontScale(1);
            float limit = work.visible() ? 590 : 640;
            if (actor.getPrefWidth() > limit) actor.setFontScale(limit / actor.getPrefWidth());
        }
        float width = actor.getPrefWidth();
        actor.setBounds(woodCenter() - width / 2, y + verticalOffset(), width, actor.getPrefHeight());
    }

    private void layoutActors() {
        if (backgroundActor == null) return;
        backgroundActor.setSize(stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        titleActor.setPosition(45, stage.getViewport().getWorldHeight() - 70);
        centered(captionActor, 674);
        centered(countActor, 567);
        centered(todayActor, 547);
        float reaction = hitAnimation / .14f, scale = 1 - .025f * reaction, w = 460 * scale, h = 337 * scale;
        fishActor.setBounds(woodCenter() - w / 2, 240 + verticalOffset() + (337 - h) / 2 - 3 * reaction, w, h);
        stickActor.setBounds(woodCenter() + stroke.headX() - 27, stroke.headY() + verticalOffset() - 30, 150, 60);
        stickActor.setRotation(stroke.rotation());
        hintActor.setText((wallpaper ? desktopHint : hint).getText());
        centered(hintActor, 223);
        centered(sessionActor, 129);
        centered(footerActor, 87);
        workActors.setVisible(work.visible());
        workActors.setBounds(cardX(), cardY(), WorkDashboard.WIDTH, WorkDashboard.HEIGHT);

    }

    private void update(float delta) {
        delta = Math.min(delta, .1f);
        progress.rollDay(LocalDate.now());
        if (automatic) {
            automaticElapsed += delta;
            if (automaticElapsed >= progress.interval) {
                automaticElapsed %= progress.interval;
                hit();
            }
        }
        hitAnimation = Math.max(0, hitAnimation - delta);
        int contacts = stroke.update(delta);
        for (int i = 0; i < contacts; i++) contact();
        if (saveDelay >= 0 && (saveDelay -= delta) <= 0) {
            save();
            saveDelay = -1;
        }
        updateLabels();
        layoutActors();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        layoutActors();
    }

    @Override
    public void dispose() {
        automatic = false;
        save();
        if (sound != null) sound.dispose();
        work.dispose();
        Gdx.input.setInputProcessor(null);
        super.dispose();
        assets.close();
    }
}
