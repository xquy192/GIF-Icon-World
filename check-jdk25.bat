@echo off
setlocal EnableExtensions DisableDelayedExpansion
cd /d "%~dp0"
echo GIF Icon World - JDK diagnostic
echo.
if not defined JAVA_HOME (
  echo JAVA_HOME is not set. The build helper will scan common locations.
) else (
  echo JAVA_HOME=%JAVA_HOME%
)
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-tools\find-jdk25.ps1"
if errorlevel 1 (
  echo [INFO] No JDK 25+ was found by the automatic scan.
) else (
  echo [OK] A compatible JDK was found.
)
echo.
echo Diagnostic complete. Press any key to close.
pause >nul
exit /b 0
