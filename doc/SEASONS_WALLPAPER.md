# 四季节气壁纸

参考春夏秋冬树木和水面倒影，使用项目内 `assets/wallpapers` 的 PNG 素材。纹理由 `Asset.getAsset().getTexture()` 加载，动态文字使用 Asset 加载的 BitmapFont 与 Label。自动模式显示今天所属季节的整幅风景；控制器可预览全部二十四节气。

## 启动

双击 `bat/seasons.bat`，或运行免安装版 `desktop/build/seasons-app/SeasonsWallpaper/SeasonsWallpaper.exe`。

控制器点击“设为桌面壁纸”即可嵌入 Windows 桌面。关闭控制器会保留托盘图标，双击图标重新打开；托盘菜单“退出四季壁纸”退出并还原桌面。Esc 从壁纸返回窗口，在窗口中按 Esc 退出；F1 打开控制器。

也可直接启动到桌面：`bat/seasons.bat --wallpaper`。

## 自动变化

- 二十四节气按太阳视黄经每 15° 的交点估算，当所在地日历进入节气当天时切换。现代年份精度约为数分钟，属于近似历算。立春、立夏、立秋、立冬分别开启四季，小寒和大寒仍为冬季。
- 春季粉色花瓣、夏季绿荫、秋季金红落叶、冬季雪枝；同一季节内随节气调整花叶密度。
- 天气来自 Open-Meteo，每 15 分钟更新；晴天无云、多云、阴天、雨、雪、雾及雷雨分别显示对应效果。冬季树木覆雪是季节画面，空中下雪只在真实雪天或雪天预览时出现。
- 太阳/月亮、夜色与星光跟随配置地点的昼夜；水面倒影与涟漪、花瓣与落叶持续轻缓运动。
- 默认地点是香港，可搜索城市，也可直接输入经纬度和 IANA 时区，例如 `Asia/Shanghai`。默认配置均为自动；预览选择会保存，恢复自动需将节气、天气、昼夜均选回“自动/实时”。
- 天气获取失败时显示缓存并标明状态；没有缓存时显示默认场景及“天气待更新”，不会把默认数据标成实时天气。

配置保存于 `%LOCALAPPDATA%/DesktopWallpaper/seasons/settings.json`。复用项目天气服务的地点校验缓存 `%LOCALAPPDATA%/DesktopWallpaper/weather-city/weather-cache.json`。

## 开发与验证

使用 JDK 21+：

```powershell
.\gradlew.bat :desktop:runSeasons --console=plain
.\gradlew.bat :desktop:test :desktop:snapshotSeasons --console=plain
.\gradlew.bat :desktop:packageSeasons --console=plain
```

截图位于 `desktop/build/seasons-preview`，包含四季、晴天、多云、阴天、雨、雪、雾、夜晚。免安装版包含 Java 运行时，无需使用者另装 Java。再次打包应先将旧免安装目录移至其他位置，避免 jpackage 覆盖已有输出。
