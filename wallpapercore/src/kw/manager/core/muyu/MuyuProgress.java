package kw.manager.core.muyu;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Properties;

/**
 * Persistent counts are separate from the wallpaper resources.
 */
public final class MuyuProgress {
    public volatile long total, today;
    public volatile boolean sound = true;
    public volatile float interval = 1.2f;
    private String date = LocalDate.now().toString();
    private final Path file;

    public MuyuProgress(Path file) {
        this.file = file;
        load();
        rollDay(LocalDate.now());
    }

    public static Path defaultFile() {
        return kw.manager.core.WallpaperStorage.root().toPath().resolve("DesktopWallpaper/muyu/progress.properties");
    }

    public void rollDay(LocalDate day) {
        if (!date.equals(day.toString())) {
            date = day.toString();
            today = 0;
        }
    }

    public void hit(LocalDate day) {
        rollDay(day);
        if (total < Long.MAX_VALUE) {
            total++;
            today++;
        }
    }

    private void load() {
        try (InputStream in = Files.newInputStream(file)) {
            Properties p = new Properties();
            p.load(in);
            total = Math.max(0, Long.parseLong(p.getProperty("total", "0")));
            today = Math.min(total, Math.max(0, Long.parseLong(p.getProperty("today", "0"))));
            date = p.getProperty("date", date);
            sound = Boolean.parseBoolean(p.getProperty("sound", "true"));
            interval = Float.parseFloat(p.getProperty("interval", "1.2"));
            if (!Float.isFinite(interval)) interval = 1.2f;
            interval = Math.max(.4f, Math.min(3, interval));
        } catch (Exception ignored) {
        }
    }

    public void save() throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        Properties p = new Properties();
        p.setProperty("total", Long.toString(total));
        p.setProperty("today", Long.toString(today));
        p.setProperty("date", date);
        p.setProperty("sound", Boolean.toString(sound));
        p.setProperty("interval", Float.toString(interval));
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try (OutputStream out = Files.newOutputStream(temp)) {
            p.store(out, "Muyu wallpaper");
        }
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
