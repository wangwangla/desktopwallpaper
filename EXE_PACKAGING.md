# EXE 打包指南（Windows）

本项目已经加好 Gradle 打包任务，可以直接从 `desktop` 模块生成 EXE 安装包。

## 1) 先决条件

- 操作系统：Windows
- JDK：推荐 **17**（必须包含 `jpackage`）；Gradle 9.3.1 也支持 JDK 25
- 项目根目录：`D:\java\desktopwallpaper`

> 项目已升级到 Gradle 9.3.1，当前电脑已有该版本的 Wrapper 缓存，无需重新下载 Gradle。Gradle 9.3.1 至少需要 JDK 17，不能再用 JDK 11 启动构建。

## 2) 已添加的打包任务

已在 `desktop/build.gradle` 中新增：

- `:desktop:dist`：生成可运行 fat-jar
- `:desktop:packageAppImage`：生成免安装目录版
- `:desktop:packageExe`：生成 Windows EXE 安装包
- `:desktop:printPackageHelp`：输出打包提示

默认入口类：`wk.com.test.WallManagerLauncher`

## 3) 一次性打包 EXE（推荐流程）

在项目根目录执行：

```powershell
# 1. 切换到 JDK 17+（把路径改成你自己的）
$env:JAVA_HOME = 'C:\Users\a\.jdks\ms-17.0.20.1'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

# 2. 确认 jpackage 可用
java -version
jpackage --version

# 3. 生成 jar
.\gradlew.bat :desktop:dist --console=plain

# 4. 生成 EXE 安装包
.\gradlew.bat :desktop:packageExe --console=plain
```

生成结果目录：`desktop/build/exe`

如果在 CMD 中执行，环境变量要使用 CMD 语法：

```bat
cd /d D:\java\desktopwallpaper
set "JAVA_HOME=C:\Users\a\.jdks\ms-17.0.20.1"
set "PATH=%JAVA_HOME%\bin;%PATH%"
gradlew.bat --version
gradlew.bat :desktop:dist --console=plain
```

JDK 17 的 `--type exe` 安装包还需要安装 WiX Toolset 3.x 并配置 PATH；免安装版 `--type app-image` 不需要 WiX。

### 打包木鱼免安装版

现有 Gradle 打包任务默认生成壁纸管理器。木鱼使用不同的名称和入口，先执行 `:desktop:dist`，再在 PowerShell 中执行：

```powershell
& "$env:JAVA_HOME\bin\jpackage.exe" `
  --type app-image `
  --name Muyu `
  --app-version 1.0 `
  --input 'D:\java\desktopwallpaper\desktop\build\libs' `
  --main-jar desktop-1.0.jar `
  --main-class wk.com.test.MuyuLauncher `
  --dest 'D:\java\desktopwallpaper\desktop\build\muyu-package'
```

输出为 `desktop/build/muyu-package/Muyu`。整个目录一起压缩发布。再次打包时，先移走已有输出目录。

## 4) 输出物说明

- `desktop/build/libs/desktop-1.0.jar`：fat-jar
- `desktop/build/exe/DesktopWallpaperManager`：app-image 或安装输出目录
- `desktop/build/exe/*.exe`：安装程序（`packageExe` 成功时）

## 5) 常见问题

### A. 报错：`jpackage` 不是内部或外部命令

原因：当前 JDK 版本过低（通常是 JDK 8/11）或未切到正确 `JAVA_HOME`。

处理：

```powershell
$env:JAVA_HOME = 'C:\Users\a\.jdks\ms-17.0.20.1'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
jpackage --version
```

### B. EXE 能打开，但资源丢失（图片/CSV 找不到）

项目已经将资源目录优先指向 `../assets`。如果你后续改过目录，请确认：

- 资源在 `assets/` 下
- 执行的是 `:desktop:dist` 后再打包

### C. 杀毒软件拦截或弹窗

未签名的 EXE 在 Windows 上可能触发 SmartScreen。属于常见现象，正式发布时建议加代码签名。

## 6) 你可以这样给别人发包

- 给安装版：发送 `desktop/build/exe` 目录里的 `.exe`
- 给免安装版：发送 `packageAppImage` 生成的目录（整个目录一起发）

## 7) 快速自检

```powershell
# 查看可用打包任务
.\gradlew.bat :desktop:tasks --all --console=plain

# 查看打包帮助
.\gradlew.bat :desktop:printPackageHelp --console=plain
```

