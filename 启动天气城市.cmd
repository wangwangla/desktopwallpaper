@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-weather-city.ps1"
if errorlevel 1 pause
