@echo off
setlocal

title Muyu Packaging

echo =====================================
echo        Muyu Application Build
echo =====================================

echo.
echo [1/2] Building LibGDX Desktop...

call "%~dp0gradlew.bat" :desktop:dist --console=plain

if errorlevel 1 goto BUILD_FAILED

echo.
echo [2/2] Packaging Muyu...

set "JPACKAGE=C:\Users\a\.jdks\ms-17.0.20.1\bin\jpackage.exe"
set "INPUT=%~dp0desktop\build\libs"
set "DEST=%~dp0desktop\build\muyu-package"

if not exist "%JPACKAGE%" goto JPACKAGE_NOT_FOUND

if not exist "%INPUT%\desktop-1.0.jar" goto JAR_NOT_FOUND

if not exist "%DEST%" mkdir "%DEST%"

"%JPACKAGE%" ^
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

:BUILD_FAILED
echo.
echo [ERROR] Gradle build failed!
pause
exit /b 1

:JPACKAGE_NOT_FOUND
echo.
echo [ERROR] jpackage.exe not found!
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
