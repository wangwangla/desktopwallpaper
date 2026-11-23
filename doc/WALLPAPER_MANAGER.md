# 统一壁纸控制器

双击 `bat/wallpaper-manager.bat`，或启动免安装版 `desktop/build/manager-app/DesktopWallpaperManager/DesktopWallpaperManager.exe`。

## 壁纸列表

当前项目的三个壁纸自动显示在列表中：一念木鱼、天气城市、四季节气。每张卡片展示实际预览图、名称、功能说明和运行状态，顶部显示当前使用的桌面壁纸。

- **设为壁纸**：先启动并确认新壁纸可用，再正常退出原桌面壁纸，最后嵌入桌面；嵌入失败时尝试恢复原壁纸。
- **窗口预览**：启动或将该壁纸切回窗口。
- **壁纸设置**：打开各自已有的中文控制器。
- **停止**：退出对应壁纸并保存其原有数据；**停止全部壁纸**退出列表里已识别的所有运行壁纸。
- **刷新列表**：重新发现现有壁纸和进程；正常使用时每两秒自动更新。搜索框按名称或功能筛选。
- **添加启动器**：选择额外的 EXE 或 JAR，并填写显示名称。外部启动器可列出、启动和停止；设置桌面的操作由它自己的控制器负责。使用 JAR 需要本机 Java，免安装版的三个内置壁纸无需另装 Java。

关闭控制器窗口会保留系统托盘。双击托盘图标重新打开；“退出管理器（保留壁纸）”只退出管理器。壁纸独立运行，管理器重启后会重新识别它们。重复启动管理器或已接入接口的壁纸会打开已有实例。

旧版本的独立启动器会按进程和窗口识别，支持打开设置和正常退出。将它切换后再次启动，会使用免安装包里的新版启动器接入统一控制。

## 开发接入

项目壁纸保留各自的启动器。在 `wk.com.test` 包中的 `*Launcher` 类上增加 `@WallpaperDefinition` 元数据后，控制器自动发现该类。启动器使用 `WallpaperSession` 接收 `wallpaper`、`preview`、`controls`、`stop` 命令，将命令派发到自身渲染线程，并在退出时关闭会话。单纯添加注解只能显示，控制操作还需接入会话接口。

三个内置壁纸已完成接入。`WallpaperRuntime` 优先使用会话控制，旧版本按名称匹配进程并向所属 GLFW 窗口发送正常关闭请求，不终止其他 Java 程序。

控制接口只监听本机回环地址，以随机令牌校验请求；注册文件包含 PID 和进程启动时间，避免将陈旧 PID 当作正在运行的壁纸。配置、附加启动器、运行会话和日志保存在 `%LOCALAPPDATA%/DesktopWallpaper/manager`。

## 构建

JDK 21+：

```powershell
.\gradlew.bat :desktop:runWallManager --console=plain
.\gradlew.bat :desktop:test :desktop:snapshotWallManager --console=plain
.\gradlew.bat :desktop:packageWallManager --console=plain
```

免安装包共享一套运行时，包含管理器，以及 Muyu、WeatherCity、SeasonsWallpaper 三个伴随启动器。使用时保留整个目录。`packageAppImage` 和 `packageExe` 也包含这些启动器。

界面预览：`desktop/build/wallpaper-manager-preview.png`。重复 jpackage 打包前应先移走旧输出目录。
