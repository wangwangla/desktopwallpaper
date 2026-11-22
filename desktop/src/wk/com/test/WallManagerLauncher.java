package wk.com.test;

import kw.wallpaper.*;

import javax.swing.*;

import java.nio.file.Path;
import java.util.Arrays;

public final class WallManagerLauncher {
    public static void main(String[] args) throws Exception {
        if(Arrays.asList(args).contains("--restore-wallpaper")){
            try{WallpaperAutostart.restore();}catch(Exception e){
                java.nio.file.Files.writeString(WallpaperCatalog.directory().resolve("startup-error.log"),java.time.Instant.now()+" "+e+System.lineSeparator(),java.nio.file.StandardOpenOption.CREATE,java.nio.file.StandardOpenOption.APPEND);
            }return;
        }
        if(Arrays.asList(args).contains("--sync-autostart")){WallpaperAutostart.synchronizeCurrent();System.out.println("Autostart: "+WallpaperAutostart.selectedWallpaper()+" / "+WallpaperAutostart.isEnabled());return;}
        if (Arrays.asList(args).contains("--list")) {
            var entries = WallpaperCatalog.discover();
            var running = new WallpaperRuntime().running(entries);
            for (var e : entries)
                System.out.println(e.id() + " | " + e.title() + " | " + running.stream().filter(r -> r.entry().id().equals(e.id())).map(r -> r.mode() + " (PID " + r.pid() + ")").findFirst().orElse("stopped"));
            return;
        }
        boolean snapshot = args.length == 2 && args[0].equals("--snapshot");
        if (!snapshot && WallpaperSession.forwardExisting("manager", args)) return;
        SwingUtilities.invokeLater(() -> {
            try {
                WallpaperManager manager = new WallpaperManager(snapshot);
                if (snapshot) manager.snapshot(Path.of(args[1]));
                else manager.show();
            } catch (Exception error) {
                error.printStackTrace();
                JOptionPane.showMessageDialog(null, error.getMessage(), "壁纸管理器", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
