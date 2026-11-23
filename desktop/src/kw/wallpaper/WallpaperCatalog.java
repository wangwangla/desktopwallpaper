package kw.wallpaper;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

public final class WallpaperCatalog {
    public record Entry(String id, String title, String description, int accent, String thumbnail,
                        String mainClass, String sceneTitle, String controllerTitle,
                        List<String> executables, Path external) {
        public boolean builtIn() {
            return external == null;
        }
    }

    public static Path directory() {
        String root = System.getenv("LOCALAPPDATA");
        Path dir = Path.of(root == null ? System.getProperty("user.home") : root, "DesktopWallpaper", "manager");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return dir;
    }

    public static List<Entry> discover() throws IOException {
        SortedSet<String> names = new TreeSet<>();
        String resource = "wk/com/test";
        Enumeration<URL> urls = WallpaperCatalog.class.getClassLoader().getResources(resource);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            if (url.getProtocol().equals("file"))
                try (var files = Files.list(Path.of(url.toURI()))) {
                    files.map(p -> p.getFileName().toString()).filter(n -> n.endsWith("Launcher.class") && !n.contains("$")).forEach(n -> names.add("wk.com.test." + n.substring(0, n.length() - 6)));
                } catch (URISyntaxException e) {
                    throw new IOException(e);
                }
            else if (url.getProtocol().equals("jar")) {
                JarURLConnection connection = (JarURLConnection) url.openConnection();
                connection.setUseCaches(false);
                try (JarFile jar = connection.getJarFile()) {
                    jar.stream().map(JarEntry::getName).filter(n -> n.startsWith(resource + "/") && n.endsWith("Launcher.class") && !n.contains("$")).forEach(n -> names.add(n.substring(0, n.length() - 6).replace('/', '.')));
                }
            }
        }
        List<Entry> entries = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (String name : names)
            try {
                Class<?> type = Class.forName(name, false, WallpaperCatalog.class.getClassLoader());
                WallpaperDefinition d = type.getAnnotation(WallpaperDefinition.class);
                if (d != null) {
                    if (!ids.add(d.id())) throw new IOException("重复的壁纸 ID：" + d.id());
                    entries.add(new Entry(d.id(), d.title(), d.description(), d.accent(), d.thumbnail(), name, d.sceneTitle(), d.controllerTitle(), List.of(d.executables()), null));
                }
            } catch (ClassNotFoundException | LinkageError e) {
                throw new IOException("无法加载壁纸入口：" + name, e);
            }
        Path extra = directory().resolve("launchers");
        Files.createDirectories(extra);
        try (var files = Files.list(extra)) {
            for (Path path : files.filter(p -> p.toString().endsWith(".properties")).sorted().toList()) {
                Properties p = new Properties();
                try (Reader reader = Files.newBufferedReader(path)) {
                    p.load(reader);
                }
                String id = p.getProperty("id");
                if (id == null || !id.matches("external-[a-f0-9-]+")) continue;
                Path executable = Path.of(p.getProperty("path")).toAbsolutePath();
                entries.add(new Entry(id, p.getProperty("title", executable.getFileName().toString()), "已添加的独立启动器", 0x687895, "", "", "", "", List.of(), executable));
            }
        }
        entries.sort(Comparator.comparing(Entry::id));
        return List.copyOf(entries);
    }

    public static Entry add(Path executable, String title) throws IOException {
        executable = executable.toAbsolutePath().normalize();
        if (!Files.isRegularFile(executable)) throw new IOException("启动器不存在");
        String lower = executable.toString().toLowerCase(Locale.ROOT);
        if (!lower.endsWith(".exe") && !lower.endsWith(".jar"))
            throw new IOException("请选择 EXE 或 JAR 启动器");
        for (Entry entry : discover()) if (executable.equals(entry.external())) return entry;
        String id = "external-" + UUID.randomUUID();
        Properties p = new Properties();
        p.setProperty("id", id);
        p.setProperty("title", title);
        p.setProperty("path", executable.toString());
        Path folder = directory().resolve("launchers");
        Files.createDirectories(folder);
        try (Writer writer = Files.newBufferedWriter(folder.resolve(id + ".properties"))) {
            p.store(writer, "Wallpaper launcher");
        }
        return discover().stream().filter(e -> e.id().equals(id)).findFirst().orElseThrow();
    }
}
