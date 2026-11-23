package kw.wallpaper;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/**
 * Authenticated, local-only control of renderer processes. Commands run on their own UI thread.
 */
public final class WallpaperSession implements AutoCloseable {
    public record Endpoint(long pid, long started, int port, String token) {
    }

    private final ServerSocket server;
    private final FileChannel lockChannel;
    private final FileLock lock;
    private final Path registration;
    private final String token = UUID.randomUUID().toString();
    private volatile boolean closed;

    public WallpaperSession(String id, Supplier<String> state, Consumer<Runnable> dispatch, Consumer<String> action) throws IOException {
        Path folder = folder();
        registration = folder.resolve(id + ".properties");
        lockChannel = FileChannel.open(folder.resolve(id + ".lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        FileLock acquired;
        try {
            acquired = lockChannel.tryLock();
        } catch (OverlappingFileLockException e) {
            acquired = null;
        }
        if (acquired == null) {
            lockChannel.close();
            throw new IOException("这个壁纸已经运行");
        }
        lock = acquired;
        ServerSocket created;
        try {
            created = new ServerSocket(0, 8, InetAddress.getByName("127.0.0.1"));
        } catch (IOException e) {
            lock.release();
            lockChannel.close();
            throw e;
        }
        server = created;
        Properties p = new Properties();
        p.setProperty("pid", "" + ProcessHandle.current().pid());
        p.setProperty("started", "" + start(ProcessHandle.current()));
        p.setProperty("port", "" + server.getLocalPort());
        p.setProperty("token", token);
        try {
            Path temp = Files.createTempFile(folder, id, ".tmp");
            try (Writer w = Files.newBufferedWriter(temp)) {
                p.store(w, "Local wallpaper session");
            }
            Files.move(temp, registration, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            server.close();
            lock.release();
            lockChannel.close();
            throw e;
        }
        Thread worker = new Thread(() -> {
            while (!closed) try (Socket socket = server.accept()) {
                socket.setSoTimeout(5000);
                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                if (!token.equals(in.readUTF())) {
                    out.writeUTF("error:访问被拒绝");
                    continue;
                }
                String command = in.readUTF();
                if (command.equals("status")) {
                    out.writeUTF(state.get());
                    continue;
                }
                if (!Set.of("wallpaper", "preview", "controls", "stop").contains(command)) {
                    out.writeUTF("error:不支持的操作");
                    continue;
                }
                CompletableFuture<String> result = new CompletableFuture<>();
                dispatch.accept(() -> {
                    try {
                        action.accept(command);
                        result.complete("ok");
                    } catch (Exception e) {
                        result.complete("error:" + e.getMessage());
                    }
                });
                try {
                    out.writeUTF(result.get(5, TimeUnit.SECONDS));
                } catch (Exception e) {
                    out.writeUTF("error:壁纸没有及时响应");
                }
            } catch (IOException e) {
                if (!closed) System.err.println("Wallpaper control: " + e.getMessage());
            }
        }, "wallpaper-control-" + id);
        worker.setDaemon(true);
        worker.start();
    }

    private static Path folder() throws IOException {
        Path folder = WallpaperCatalog.directory().resolve("sessions");
        Files.createDirectories(folder);
        return folder;
    }

    private static long start(ProcessHandle process) {
        return process.info().startInstant().map(t -> t.toEpochMilli()).orElse(0L);
    }

    public static Optional<Endpoint> endpoint(String id) {
        try {
            Properties p = new Properties();
            try (Reader r = Files.newBufferedReader(folder().resolve(id + ".properties"))) {
                p.load(r);
            }
            Endpoint e = new Endpoint(Long.parseLong(p.getProperty("pid")), Long.parseLong(p.getProperty("started")), Integer.parseInt(p.getProperty("port")), p.getProperty("token"));
            ProcessHandle process = ProcessHandle.of(e.pid()).orElse(null);
            if (process == null || !process.isAlive() || start(process) != e.started())
                return Optional.empty();
            return Optional.of(e);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static String request(Endpoint endpoint, String command) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", endpoint.port()), 700);
            socket.setSoTimeout(6000);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(endpoint.token());
            out.writeUTF(command);
            out.flush();
            String result = new DataInputStream(socket.getInputStream()).readUTF();
            if (result.startsWith("error:")) throw new IOException(result.substring(6));
            return result;
        }
    }

    public static boolean forwardExisting(String id, String[] args) {
        if (Arrays.stream(args).anyMatch(a -> a.contains("snapshot"))) return false;
        Optional<Endpoint> e = endpoint(id);
        if (e.isEmpty()) return false;
        try {
            request(e.get(), Arrays.asList(args).contains("--wallpaper") ? "wallpaper" : "controls");
            return true;
        } catch (IOException error) {
            return false;
        }
    }

    @Override
    public void close() {
        closed = true;
        try {
            Properties p = new Properties();
            if (Files.exists(registration)) {
                try (Reader r = Files.newBufferedReader(registration)) {
                    p.load(r);
                }
                if (token.equals(p.getProperty("token"))) Files.deleteIfExists(registration);
            }
        } catch (IOException ignored) {
        }
        try {
            server.close();
            lock.release();
            lockChannel.close();
        } catch (IOException ignored) {
        }
    }
}
