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

## 开机启动

设置任意内置壁纸后，自动记住该壁纸并开启当前 Windows 用户登录后的自启。开机时直接恢复上次壁纸，控制器窗口不弹出；仍可从托盘打开设置。

切换壁纸会更新自启目标。停止当前壁纸、停止全部或把当前壁纸切回窗口预览，会取消自启；停止其他预览不影响当前壁纸的自启。关闭统一控制器会保留壁纸及自启。系统注销或关机时正常释放壁纸窗口不会清除选择，因此下次登录仍会恢复。

启动项仅使用 `HKCU/Software/Microsoft/Windows/CurrentVersion/Run` 下的 `DesktopWallpaper` 值，调用统一控制器的 `--restore-wallpaper` 模式；选择记录在 `manager/startup.properties`。保留免安装目录原位置，移动后重新设置一次壁纸即可更新路径。

## 开发接入

core 使用 Game + Screen：`WallpaperGame` 继承现有的 `com.kw.gdx.BaseGame`，`MuyuGame`、`WeatherCityGame`、`SeasonsGame` 各自创建继承 `com.kw.gdx.screen.BaseScreen` 的 Screen。画面、动画、天气、节气与业务设置全部在 `wallpapercore/src/kw/manager/core`。

三个 Screen 直接继承 `BaseScreen.render()`，不再单独维护 SpriteBatch、Viewport 或清屏。木鱼的图像、文字和反馈通过 Image Actor 显示，木鱼及作息卡片使用 ClickListener/Button；Stage 负责输入命中和事件分发。动画和业务更新由 Actor.act 驱动。四季背景使用 Image Actor；天气城市通过 CitySceneActor 衔接专用 3D ModelBatch，由 Stage 管理显示顺序。桌面鼠标适配器只转换 Windows 坐标并转发按下/松开，不计算木鱼或卡片命中区域。

所有壁纸素材位于 `assets/wallpapers`：木鱼/宠物、24 个节气背景、天气图标、建筑贴图和 32 张月相图片均为 PNG。`WallpaperAssets` 通过现有 `Asset.getAsset().getTexture()` 加载纹理，通过 `Asset.loadBitFont()` 加载 FNT 字体；Screen 退出时通过 Asset 卸载，多个使用方共享的资源在最后一方释放时才卸载。动态数字、时间、收入和提示使用 Label，运行时不再绘制 BufferedImage/Pixmap 或创建贴图。

旧素材绘制代码移到 `tools/artwork/src/kw/artwork`，仅作为离线素材导出工具。正常运行和打包不调用生成器；PNG 可以直接替换。需要重新导出原始素材时可单独执行 `:desktop:bakeWallpaperAssets`，完成后再执行正常构建。

`WallpaperPlatform` 是 core 对桌面能力的回调接口，包括设置壁纸、窗口预览、退出、打开设置，以及 ready/frame/disposed 生命周期通知。core 不引用 LWJGL、JNA、Swing、托盘或启动项。Game 负责 BaseGame/Screen 生命周期，退出时释放 Screen 和共享 Batch。

desktop 的 `DesktopWallpaperHost` 实现回调，集中管理 GLFW 窗口、Windows 桌面嵌入、IPC、自启和托盘设置窗口；原来的三个 Launcher 只配置窗口、创建对应的 core Game 并绑定桌面设置。Swing 设置窗口位于 `desktop/src/kw/desktop/controls`。截图工具在 desktop 捕获帧缓冲，通过 Screen 的预览接口选择测试画面。

项目壁纸保留各自的启动器。在 `wk.com.test` 包中的 `*Launcher` 类上增加 `@WallpaperDefinition` 元数据后，控制器自动发现该类。新壁纸接入 `DesktopWallpaperHost` 和 `WallpaperPlatform` 即可使用统一控制。免安装打包时还需在 `prepareManagerInput` 中注册对应的伴随启动器。

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
