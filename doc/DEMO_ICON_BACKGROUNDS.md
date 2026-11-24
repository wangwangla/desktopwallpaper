# 桌面图标背景 Demo

使用统一壁纸控制器中的“图标背景 Demo”，或执行 `:desktop:runDemo`。点击“设为壁纸”后，主屏桌面图标后方会出现半透明背景。原有桌面图标仍由 Windows 处理点击和拖动。

desktop 每 180 毫秒读取 Explorer 桌面 ListView 的图标边界，通过 `WallpaperPlatform.desktopIcons()` 返回不可变快照。core 为每个图标维护一个 Image Actor，纹理来自 Asset 加载的 `wallpapers/ui/rounded.png`。图标位置变更时使用 0.4 秒的 sineOut 移动/缩放 Action；同一目标的重复快照不重启动画。删除图标后下一次有效快照移除其 Actor；隐藏桌面图标时也清空背景。

图标身份优先使用 Explorer 的项目标识，查询失败时使用名称和重名序号。读取过程使用临时远程查询缓冲区，并在每次查询后释放；不修改桌面文件或图标位置。读取失败时保留上一份有效位置，Explorer 恢复后继续更新。多屏时当前只显示主屏壁纸窗口覆盖范围内的图标。

验证命令（JDK 21+）：

```powershell
.\gradlew.bat :desktop:test :desktop:inspectDesktopIcons :desktop:snapshotDemo --console=plain
.\gradlew.bat :desktop:runDemo -Pwallpaper --console=plain
```

`snapshotDemo` 使用示例图标位置做画面检查，`inspectDesktopIcons` 读取真实 Windows 桌面。Demo 已接入统一控制、窗口预览、退出和开机恢复；若不希望下次登录恢复 Demo，可切换回其他壁纸或停止 Demo。
