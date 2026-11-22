package kw.wallpaper;

import com.sun.jna.platform.win32.*;
import java.io.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Supplier;

/** Keep the selected desktop wallpaper, rather than a preview or the manager window, at sign-in. */
public final class WallpaperAutostart {
    public static final String RUN_KEY="Software\\Microsoft\\Windows\\CurrentVersion\\Run";
    public static final String RUN_NAME="DesktopWallpaper";
    interface StartupEntry {void write(String command)throws IOException;void remove()throws IOException;boolean exists();}
    private static final class WindowsEntry implements StartupEntry {
        public void write(String command)throws IOException {try{Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER,RUN_KEY);Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER,RUN_KEY,RUN_NAME,command);}catch(RuntimeException e){throw new IOException("无法设置开机启动",e);}}
        public void remove()throws IOException {try{if(exists())Advapi32Util.registryDeleteValue(WinReg.HKEY_CURRENT_USER,RUN_KEY,RUN_NAME);}catch(RuntimeException e){throw new IOException("无法取消开机启动",e);}}
        public boolean exists(){try{return Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER,RUN_KEY,RUN_NAME);}catch(RuntimeException e){return false;}}
    }
    private static final class Default {static final WallpaperAutostart INSTANCE=new WallpaperAutostart(WallpaperCatalog.directory().resolve("startup.properties"),new WindowsEntry(),WallpaperAutostart::command);}
    private final Path state;
    private final StartupEntry startup;
    private final Supplier<String> command;
    WallpaperAutostart(Path state,StartupEntry startup,Supplier<String> command){this.state=state;this.startup=startup;this.command=command;}
    private String read()throws IOException {
        if(!Files.exists(state))return "";Properties p=new Properties();try(Reader r=Files.newBufferedReader(state)){p.load(r);}return p.getProperty("wallpaper","");
    }
    private void save(String id)throws IOException {
        Properties p=new Properties();p.setProperty("wallpaper",id);Path tmp=Files.createTempFile(state.getParent(),"startup-",".tmp");
        try{try(Writer w=Files.newBufferedWriter(tmp)){p.store(w,"Selected desktop wallpaper for sign-in");}Files.move(tmp,state,StandardCopyOption.REPLACE_EXISTING);}
        finally{Files.deleteIfExists(tmp);}
    }
    @FunctionalInterface private interface Change {void apply()throws IOException;}
    private synchronized void locked(Change change)throws IOException {
        Files.createDirectories(state.getParent());try(FileChannel channel=FileChannel.open(state.resolveSibling(state.getFileName()+".lock"),StandardOpenOption.CREATE,StandardOpenOption.WRITE);FileLock lock=channel.lock()){change.apply();}
    }
    void enable(String id)throws IOException {
        if(!id.matches("[a-z][a-z0-9-]*"))throw new IOException("无效的壁纸标识");
        String restoreCommand=command.get();
        locked(()->{String before=read();save(id);try{startup.write(restoreCommand);}catch(IOException e){save(before);throw e;}});
    }
    void disable(String id)throws IOException {locked(()->{String selected=read();if(id==null||selected.equals(id)){startup.remove();save("");}});}
    synchronized String selected(){try{return read();}catch(IOException e){return "";}}
    boolean enabled(){return !selected().isEmpty()&&startup.exists();}
    public static void select(String id){try{Default.INSTANCE.enable(id);}catch(IOException e){throw new UncheckedIOException(e);}}
    public static void clear(String id){try{Default.INSTANCE.disable(id);}catch(IOException e){throw new UncheckedIOException(e);}}
    public static String selectedWallpaper(){return Default.INSTANCE.selected();}
    public static boolean isEnabled(){return Default.INSTANCE.enabled();}
    public static String command(){
        String process=ProcessHandle.current().info().command().orElse("");
        if(!process.isEmpty()){Path manager=Path.of(process).getParent().resolve("DesktopWallpaperManager.exe");if(Files.isRegularFile(manager))return "\""+manager.toAbsolutePath()+"\" --restore-wallpaper";}
        try {
            Path base=Path.of(WallpaperAutostart.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if(!Files.isDirectory(base))base=base.getParent();
            for(int depth=0;base!=null&&depth<10;depth++,base=base.getParent()){
                Path manager=base.resolve("desktop/build/manager-app/DesktopWallpaperManager/DesktopWallpaperManager.exe");
                if(Files.isRegularFile(manager))return "\""+manager.toAbsolutePath()+"\" --restore-wallpaper";
            }
        }catch(Exception e){throw new IllegalStateException("找不到统一控制器，请先生成免安装版",e);}
        throw new IllegalStateException("找不到统一控制器，请先生成免安装版");
    }
    public static void synchronizeCurrent()throws IOException {
        var catalog=WallpaperCatalog.discover();var current=new WallpaperRuntime().running(catalog).stream().filter(r->r.entry().builtIn()&&r.mode().equals("wallpaper")).findFirst();
        if(current.isPresent())select(current.get().entry().id());else clear(null);
    }
    public static void restore()throws Exception {
        String id=selectedWallpaper();if(id.isEmpty())return;
        var catalog=WallpaperCatalog.discover();var entry=catalog.stream().filter(e->e.id().equals(id)&&e.builtIn()).findFirst();
        if(entry.isEmpty()){clear(id);return;}
        // Explorer may not yet have created its wallpaper layer during Windows sign-in.
        for(int attempt=0;attempt<45;attempt++){
            var desktop=User32.INSTANCE.FindWindow("Progman",null);
            if(desktop!=null&&User32.INSTANCE.IsWindowVisible(desktop))break;
            if(attempt==44)throw new IOException("登录后仍未找到 Windows 桌面");Thread.sleep(1000);
        }
        new WallpaperRuntime().activate(entry.get(),catalog,true);
    }
}
