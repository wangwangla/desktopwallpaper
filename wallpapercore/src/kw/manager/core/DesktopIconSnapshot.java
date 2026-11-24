package kw.manager.core;

import java.util.List;

/**
 * Platform-neutral icon bounds, in physical pixels relative to the wallpaper window.
 */
public record DesktopIconSnapshot(int width, int height, List<Icon> icons, boolean available,
                                  String status) {
    public record Icon(String id, String name, int x, int y, int width, int height) {
    }

    public DesktopIconSnapshot {
        icons = List.copyOf(icons);
    }

    public static DesktopIconSnapshot empty() {
        return new DesktopIconSnapshot(1, 1, List.of(), true, "窗口预览：设置为壁纸后跟随桌面图标");
    }

    public static DesktopIconSnapshot pending(String status) {
        return new DesktopIconSnapshot(1, 1, List.of(), false, status);
    }
}
