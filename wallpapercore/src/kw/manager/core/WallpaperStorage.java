package kw.manager.core;
import java.io.File;
/** Hosts supply writable storage, while desktop retains its existing location. */
public final class WallpaperStorage {
    private static File directory;
    private WallpaperStorage() {}
    static void configure(WallpaperPlatform platform) { directory = platform.storageDirectory(); }
    public static File root() {
        if (directory != null) return directory;
        String local = System.getenv("LOCALAPPDATA");
        return new File(local == null || local.isEmpty() ? System.getProperty("user.home") : local);
    }
}
