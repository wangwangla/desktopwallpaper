# Android 动态壁纸

入口是 `com.joker.puzzle.AndroidLauncher`，列表包含木鱼、二十四节气和天气城市。按钮打开 Android 系统动态壁纸预览；由用户点击系统的“设置壁纸”。系统保存选中的服务并管理启动、可见性暂停与恢复，不需要 BOOT_COMPLETED 接收器。

`wallpaper/BaseWallpaperService` 使用 LibGDX `AndroidLiveWallpaperService`。Game / BaseScreen / Stage / Asset 均复用 wallpapercore；AndroidWallpaperPlatform 只负责平台导航与内部存储目录回调。三种服务分别使用独立进程，避免各壁纸的 Gdx、Asset、Constant 单例相互干扰。实时天气默认位置为香港；Demo 的 Windows 桌面图标位置接口不适用于 Android 启动器。

构建：JDK 21，执行 `gradlew.bat :android:assembleDebug`。APK 位于 `android/build/outputs/apk/debug/android-debug.apk`。Android 后端和 ARM 原生库统一使用 1.13.1；原生库由 Gradle 提取，不再使用 libs 下旧的 libgdx.so。Java 21 与 java.time / java.nio 通过 desugar_jdk_libs_nio / coreLibraryDesugaring 适配 minSdk 23。
