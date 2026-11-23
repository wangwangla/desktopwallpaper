
@echo off
setlocal

title Muyu Packaging

echo =====================================
echo        Muyu Application Build
echo =====================================

echo.
echo [1/2] Checking jpackage...

where jpackage >nul 2>nul
if errorlevel 1 goto JPACKAGE_NOT_FOUND

echo [2/2] Building LibGDX Desktop...

call "%~dp0gradlew.bat" :desktop:dist --console=plain

if errorlevel 1 goto BUILD_FAILED

echo.
echo Packaging Muyu...

set "INPUT=%~dp0desktop\build\libs"
set "DEST=%~dp0desktop\build\muyu-package"

if not exist "%INPUT%\desktop-1.0.jar" goto JAR_NOT_FOUND

if not exist "%DEST%" mkdir "%DEST%"

jpackage ^
    --type app-image ^
    --name Muyu ^
    --app-version 1.0 ^
    --input "%INPUT%" ^
    --main-jar desktop-1.0.jar ^
    --main-class wk.com.test.MuyuLauncher ^
    --dest "%DEST%"

if errorlevel 1 goto PACKAGE_FAILED

echo.
echo =====================================
echo          Packaging Success!
echo =====================================
echo.
echo Output: %DEST%\Muyu\Muyu.exe
echo.

pause
exit /b 0

:JPACKAGE_NOT_FOUND
echo.
echo [ERROR] jpackage not found in PATH!
echo Please check your environment variables.
pause
exit /b 1

:BUILD_FAILED
echo.
echo [ERROR] Gradle build failed!
pause
exit /b 1

:JAR_NOT_FOUND
echo.
echo [ERROR] desktop-1.0.jar not found!
pause
exit /b 1

:PACKAGE_FAILED
echo.
echo [ERROR] JPackage failed!
pause
exit /b 1
