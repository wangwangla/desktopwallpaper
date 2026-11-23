# AFTERHOURS / CRT 复古页面

在项目根目录启动：

```powershell
.\gradlew.bat :desktop:runRetroCrt
```

预览入口：`wk.com.test.RetroCrtLauncher`。IDE 直接运行时将工作目录设为项目的 `assets`。

正式入口已接入 `WindowGame.loadingView()`：`setScreen(new RetroCrtScreen(this))`。原来的 `WallpapgerLauncher` 会走这个页面和已有的壁纸挂载流程。上面的预览入口也运行同一个 `WindowGame`，只使用空的窗口回调，因此不会替换 Windows 壁纸。页面包含本地时钟、三个可切换面板、动态示波曲线和终端日志。

操作：

- 空格：切换 CRT / 原图。
- 左右方向键：调整强度；也可点击底部强度条。
- P：暂停或继续信号动画。
- 1 / 2 / 3 或点击侧栏：切换面板。
- R：恢复默认设置。

实现：`wallpapercore/src/kw/manager/core/RetroCrtScreen.java` 继承 `BasePetScreen`，参考 `MainScreen` 在 `initView()` 中通过 `addActor()` 添加页面。`render(delta)` 将 Stage 绘制进 FrameBuffer，在最后一次 SpriteBatch 绘制中应用 `assets/shaders/retro-crt.frag`。包含 UV 曲面畸变、边缘羽化、RGB 色偏、扫描线、荧光栅格、局部辉光、暗角和微弱闪烁。强度为零时完全直通。FrameBuffer 随窗口尺寸重建，点击位置使用同一畸变公式映射到页面坐标。桌面点击复用 `WindowGame.setMousePosition()` 的 `mousePosisiton` 事件；退出时释放 Screen、FrameBuffer、Shader 和事件监听。

页面按 1200×800 设计，窗口尺寸变化时整体缩放。波形是装饰性合成信号，不代表真实音频或设备指标。

运行 OpenGL 冒烟检查（自动退出）：

```powershell
.\gradlew.bat :desktop:runRetroCrt -PcrtSmoke
```

检查 WindowGame/Screen/Stage 渲染链、Shader、FrameBuffer、开关、快捷键、桌面点击事件、缩放和 hide/show；输出开关对比截图 `work/retro-crt-on.png` 和 `work/retro-crt-off.png`。此检查不调用 Windows 壁纸挂载接口。
