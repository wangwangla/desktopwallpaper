package kw.wallpaper;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

/**
 * Process ownership and graceful commands, including detection of older independent launchers.
 */
public final class WallpaperRuntime {
    public record Running(WallpaperCatalog.Entry entry, long pid, String mode,
                          WallpaperSession.Endpoint endpoint) {
        public boolean managed() {
            return endpoint != null;
        }
    }

    private record NativeWindow(WinDef.HWND handle, long pid, String type, String title,
                                boolean desktop) {
    }

    private final Map<String, Process> children = new ConcurrentHashMap<>();
    private final Path appDirectory;

    public WallpaperRuntime() {
        String command = ProcessHandle.current().info().command().orElse("");
        appDirectory = command.isEmpty() ? null : Path.of(command).getParent();
    }

    private static List<NativeWindow> windows() {
        if (!System.getProperty("os.name").startsWith("Windows")) return List.of();
        Map<Long, NativeWindow> result = new LinkedHashMap<>();
        WinUser.WNDENUMPROC visitor = (window, data) -> {
            IntByReference pid = new IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(window, pid);
            char[] type = new char[256], title = new char[512];
            User32.INSTANCE.GetClassName(window, type, type.length);
            User32.INSTANCE.GetWindowText(window, title, title.length);
            String name = Native.toString(type);
            if (name.equals("GLFW30") || name.equals("SunAwtFrame")) {
                WinDef.HWND parent = User32.INSTANCE.GetParent(window);
                boolean desktop = false;
                if (parent != null) {
                    char[] p = new char[256];
                    User32.INSTANCE.GetClassName(parent, p, p.length);
                    desktop = Set.of("Progman", "WorkerW").contains(Native.toString(p));
                }
                result.put(com.sun.jna.Pointer.nativeValue(window.getPointer()), new NativeWindow(window, Integer.toUnsignedLong(pid.getValue()), name, Native.toString(title), desktop));
            }
            return true;
        };
        User32.INSTANCE.EnumWindows((window, data) -> {
            visitor.callback(window, data);
            User32.INSTANCE.EnumChildWindows(window, visitor, null);
            return true;
        }, null);
        return List.copyOf(result.values());
    }

    public List<Running> running(List<WallpaperCatalog.Entry> entries) {
        List<Running> result = new ArrayList<>();
        List<NativeWindow> windows = windows();
        List<ProcessHandle> processes = ProcessHandle.allProcesses().toList();
        for (var entry : entries) {
            Optional<WallpaperSession.Endpoint> endpoint = WallpaperSession.endpoint(entry.id());
            long managedPid = -1;
            if (endpoint.isPresent()) try {
                result.add(new Running(entry, endpoint.get().pid(), WallpaperSession.request(endpoint.get(), "status"), endpoint.get()));
                managedPid = endpoint.get().pid();
            } catch (IOException ignored) {
            }
            final long alreadyManaged = managedPid;
            Set<Long> matching = processes.stream().filter(p -> p.pid() != alreadyManaged && matches(p, entry)).map(ProcessHandle::pid).collect(Collectors.toSet());
            List<NativeWindow> scene = windows.stream().filter(w -> matching.contains(w.pid()) && w.type().equals("GLFW30") && w.title().equals(entry.sceneTitle())).toList();
            if (!scene.isEmpty()) {
                for (var w : scene)
                    result.add(new Running(entry, w.pid(), w.desktop() ? "wallpaper" : "preview", null));
            } else if (!entry.builtIn()) {
                Process tracked = children.get(entry.id());
                if (tracked != null && tracked.isAlive())
                    result.add(new Running(entry, tracked.pid(), "external", null));
                else if (!matching.isEmpty())
                    result.add(new Running(entry, matching.iterator().next(), "external", null));
            }
        }
        return result;
    }

    private static boolean matches(ProcessHandle process, WallpaperCatalog.Entry entry) {
        ProcessHandle.Info info = process.info();
        String command = info.command().orElse("");
        String[] args = info.arguments().orElse(new String[0]);
        if (entry.builtIn()) {
            if (Arrays.asList(args).contains(entry.mainClass())) return true;
            if (command.isEmpty()) return false;
            String name = Path.of(command).getFileName().toString();
            return entry.executables().stream().anyMatch(e -> e.equalsIgnoreCase(name));
        }
        return command.equalsIgnoreCase(entry.external().toString()) || Arrays.asList(args).contains(entry.external().toString());
    }

    public Process launch(WallpaperCatalog.Entry entry) throws IOException {
        return launch(entry, false);
    }

    private Process launch(WallpaperCatalog.Entry entry, boolean quiet) throws IOException {
        List<String> command = new ArrayList<>();
        if (entry.builtIn()) {
            Path companion = appDirectory == null || entry.executables().isEmpty() ? null : appDirectory.resolve(entry.executables().get(0));
            if (companion != null && Files.isRegularFile(companion))
                command.add(companion.toString());
            else {
                command.add(javaExecutable());
                command.add("-Djava.net.preferIPv4Stack=true");
                command.add("-cp");
                command.add(System.getProperty("java.class.path"));
                command.add(entry.mainClass());
            }
        } else {
            if (!Files.isRegularFile(entry.external()))
                throw new IOException("这个启动器已被移动或删除，请重新添加");
            if (entry.external().toString().toLowerCase(Locale.ROOT).endsWith(".jar")) {
                command.add(javaExecutable());
                command.add("-jar");
            }
            command.add(entry.external().toString());
        }
        if (quiet && entry.builtIn()) command.add("--quiet-controls");
        ProcessBuilder builder = new ProcessBuilder(command);
        if (entry.external() != null) builder.directory(entry.external().getParent().toFile());
        builder.redirectErrorStream(true);
        builder.redirectOutput(ProcessBuilder.Redirect.appendTo(WallpaperCatalog.directory().resolve(entry.id() + ".log").toFile()));
        Process process = builder.start();
        children.put(entry.id(), process);
        return process;
    }

    private static String javaExecutable() throws IOException {
        String exe = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        Path java = Path.of(System.getProperty("java.home"), "bin", exe);
        if (Files.isRegularFile(java)) return java.toString();
        String home = System.getenv("JAVA_HOME");
        if (home != null) {
            java = Path.of(home, "bin", exe);
            if (Files.isRegularFile(java)) return java.toString();
        }
        throw new IOException("JAR 启动器需要 Java 21+；也可以添加它的 EXE 版本");
    }

    private WallpaperSession.Endpoint ensureManaged(WallpaperCatalog.Entry entry, List<WallpaperCatalog.Entry> catalog) throws Exception {
        return ensureManaged(entry, catalog, false);
    }

    private WallpaperSession.Endpoint ensureManaged(WallpaperCatalog.Entry entry, List<WallpaperCatalog.Entry> catalog, boolean quiet) throws Exception {
        for (Running current : running(catalog))
            if (current.entry().id().equals(entry.id())) {
                if (current.managed()) return current.endpoint();
                stop(current);
            }
        Process process = launch(entry, quiet);
        for (int attempt = 0; attempt < 150; attempt++) {
            if (!process.isAlive())
                throw new IOException("壁纸启动失败，日志：" + WallpaperCatalog.directory().resolve(entry.id() + ".log"));
            Optional<WallpaperSession.Endpoint> e = WallpaperSession.endpoint(entry.id());
            if (e.isPresent()) {
                Thread.sleep(350);
                WallpaperSession.request(e.get(), "status");
                return e.get();
            }
            Thread.sleep(100);
        }
        process.destroy();
        throw new IOException("壁纸启动超时，请查看日志：" + WallpaperCatalog.directory().resolve(entry.id() + ".log"));
    }

    public void preview(WallpaperCatalog.Entry entry, List<WallpaperCatalog.Entry> catalog) throws Exception {
        if (!entry.builtIn()) {
            if (running(catalog).stream().noneMatch(r -> r.entry().id().equals(entry.id())))
                launch(entry);
            return;
        }
        WallpaperSession.request(ensureManaged(entry, catalog), "preview");
        WallpaperAutostart.clear(entry.id());
    }

    public void activate(WallpaperCatalog.Entry entry, List<WallpaperCatalog.Entry> catalog) throws Exception {
        activate(entry, catalog, false);
    }

    public void activate(WallpaperCatalog.Entry entry, List<WallpaperCatalog.Entry> catalog, boolean quiet) throws Exception {
        if (!entry.builtIn()) {
            preview(entry, catalog);
            return;
        }
        WallpaperSession.Endpoint target = ensureManaged(entry, catalog, quiet);
        List<Running> previous = running(catalog).stream().filter(r -> r.pid() != target.pid() && (r.mode().equals("wallpaper") || r.entry().id().equals(entry.id()))).toList();
        // First prove the new renderer is ready, then switch; restore the old wallpaper if attaching fails.
        try {
            for (Running old : previous) stop(old);
            WallpaperSession.request(target, "wallpaper");
            WallpaperAutostart.select(entry.id());
        } catch (Exception failure) {
            for (Running old : previous)
                try {
                    WallpaperSession.request(ensureManaged(old.entry(), catalog), "wallpaper");
                } catch (Exception ignored) {
                }
            throw failure;
        }
    }

    public void controls(WallpaperCatalog.Entry entry, List<WallpaperCatalog.Entry> catalog) throws Exception {
        for (Running current : running(catalog))
            if (current.entry().id().equals(entry.id())) {
                if (current.managed()) {
                    WallpaperSession.request(current.endpoint(), "controls");
                    return;
                }
                for (var window : windows())
                    if (window.pid() == current.pid() && window.type().equals("SunAwtFrame") && (entry.controllerTitle().isEmpty() || window.title().equals(entry.controllerTitle()))) {
                        User32.INSTANCE.ShowWindow(window.handle(), WinUser.SW_RESTORE);
                        User32.INSTANCE.SetForegroundWindow(window.handle());
                        return;
                    }
                if (!entry.builtIn()) return;
            }
        if (entry.builtIn()) WallpaperSession.request(ensureManaged(entry, catalog), "controls");
        else preview(entry, catalog);
    }

    public void stop(Running current) throws Exception {
        ProcessHandle process = ProcessHandle.of(current.pid()).orElse(null);
        if (process == null || !process.isAlive()) return;
        // IDE launches may use a Java argument file, hiding the main class from ProcessHandle.
        // An authenticated session also proves identity through its PID and start time.
        boolean validIdentity = current.managed() ? WallpaperSession.endpoint(current.entry().id()).filter(e -> e.equals(current.endpoint())).isPresent() : matches(process, current.entry());
        if (!validIdentity) throw new IOException("进程已发生变化，请刷新列表");
        if (current.managed()) WallpaperSession.request(current.endpoint(), "stop");
        else {
            List<NativeWindow> owned = windows().stream().filter(w -> w.pid() == current.pid() && (current.entry().builtIn() ? w.type().equals("GLFW30") : true)).toList();
            if (!owned.isEmpty()) for (var window : owned)
                User32.INSTANCE.PostMessage(window.handle(), WinUser.WM_CLOSE, new WinDef.WPARAM(0), new WinDef.LPARAM(0));
            else if (!current.entry().builtIn()) process.destroy();
            else throw new IOException("找不到这个壁纸的窗口，请在原控制器中退出");
        }
        try {
            process.onExit().get(7, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new IOException("壁纸尚未退出，请检查它的设置窗口");
        }
        WallpaperAutostart.clear(current.entry().id());
    }

    public void stopEntry(WallpaperCatalog.Entry entry, List<WallpaperCatalog.Entry> catalog) throws Exception {
        for (Running r : running(catalog)) if (r.entry().id().equals(entry.id())) stop(r);
        WallpaperAutostart.clear(entry.id());
    }
}
