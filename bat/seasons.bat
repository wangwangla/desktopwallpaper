@echo off
setlocal
cd /d "%~dp0.."
if exist "desktop\build\seasons-app\SeasonsWallpaper\SeasonsWallpaper.exe" (
    start "" "desktop\build\seasons-app\SeasonsWallpaper\SeasonsWallpaper.exe" %*
    exit /b
)
if not defined JAVA_HOME (
    for %%D in ("%USERPROFILE%\.jdks\jbr-21*" "%USERPROFILE%\.jdks\corretto-22*") do if exist "%%~D\bin\java.exe" set "JAVA_HOME=%%~D"
)
if not defined JAVA_HOME (
    echo Please install JDK 21 or newer and set JAVA_HOME.
    pause
    exit /b 1
)
if "%~1"=="--wallpaper" (
    call gradlew.bat :desktop:runSeasons -Pwallpaper --console=plain
) else (
    call gradlew.bat :desktop:runSeasons --console=plain
)
if errorlevel 1 pause
