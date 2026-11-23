@echo off
setlocal
cd /d "%~dp0.."
if exist "desktop\build\manager-app\DesktopWallpaperManager\DesktopWallpaperManager.exe" (
    start "" "desktop\build\manager-app\DesktopWallpaperManager\DesktopWallpaperManager.exe"
    exit /b
)
if not defined JAVA_HOME (
    for %%D in ("%USERPROFILE%\.jdks\jbr-21*" "%USERPROFILE%\.jdks\corretto-22*") do if exist "%%~D\bin\java.exe" set "JAVA_HOME=%%~D"
)
call gradlew.bat :desktop:runWallManager --console=plain
if errorlevel 1 pause
