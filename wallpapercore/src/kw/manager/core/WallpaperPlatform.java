package kw.manager.core;

/** Platform services are supplied by the host; core never imports desktop window APIs. */
public interface WallpaperPlatform {
    void wallpaper();
    void preview();
    void quit();
    void showControls();
    default boolean isWallpaper(){return false;}
    default DesktopIconSnapshot desktopIcons(){return DesktopIconSnapshot.empty();}
    default void showWorkControls(){showControls();}
    default void ready(WallpaperGame<?> game){}
    default void frame(WallpaperGame<?> game){}
    default void disposed(){}
}
