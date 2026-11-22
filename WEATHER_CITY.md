# 天气城市壁纸

这是沿用 Java / LibGDX 的 3D 城区原型。城市由带立面纹理的模型组成，包含道路、树木、车辆、屋顶设备、阴影和夜间窗灯。

## 启动

使用 JDK 17 或更高版本（项目现用 Gradle 9.3.1）执行 `gradlew.bat :desktop:runWeatherCity`。

本机已提供 `启动天气城市.cmd`，优先启动 `D:\Tool\weather-city-app\WeatherCity\WeatherCity.exe`，该应用自带运行环境。

控制器可以选择天气地点、跟随当地实际时间、预览其他天气或时间，以及把场景设为桌面壁纸。窗口隐藏后，通过托盘的“打开控制器”找回设置。

默认天气地点为香港。天气地点决定天气、时区与日出日落，场景是一座示意海滨城区，并不对应所选城市的真实建筑布局。

## 数据与时间

天气来自 Open-Meteo，约十五分钟更新一次；请求失败时每分钟重试，也可在控制器手动刷新。太阳照明按经纬度和当地时间计算，日出日落优先采用接口返回的时间。断网时使用最近天气；首次离线启动会明确显示默认场景。月相使用朔望月周期近似计算。阴天会遮蔽日月和星光。

配置和天气缓存保存在 `%LOCALAPPDATA%\DesktopWallpaper\weather-city\`，不会写入项目资源。

## 模型和代码

`CityModel` 创建默认模型；`CitySky` 绘制天空和天气；`Astronomy` 计算太阳和月相；`WeatherService` 管理请求与缓存；`WeatherCityController` 提供中文控制器。

设置文件中的 `modelPath` 可以指定一个 G3DJ 模型的绝对路径。导入模型将替换默认城区。外部模型的材质、夜间灯光和性能需要针对资源进一步调整。

`gradlew.bat :desktop:snapshotWeatherCity` 会渲染白天、日出附近、雨天、夜间四种预览，输出到 `desktop/build/weather-city-preview/`，不会修改桌面。

## 资源来源

默认建筑几何和纹理由本项目生成。补充的 3D 着色器来自 LibGDX 1.12.1，遵循 Apache 2.0 许可：https://github.com/libgdx/libgdx/tree/1.12.1 。天气数据来源：https://open-meteo.com/ 。
