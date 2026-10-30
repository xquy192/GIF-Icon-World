@echo off
setlocal EnableExtensions DisableDelayedExpansion

if /i not "%~1"=="--worker" (
  start "GIF Icon World Build" "%ComSpec%" /d /k ""%~f0" --worker"
  if errorlevel 1 (
    echo [ERROR] Could not open the build window.
    pause
  )
  exit /b 0
)
shift

set "PROJECT_DIR=%GIF_ICON_WORLD_PROJECT%"
if defined PROJECT_DIR (
  for %%P in ("%PROJECT_DIR%") do set "PROJECT_DIR=%%~fP"
  if not exist "%PROJECT_DIR%\build.gradle" set "PROJECT_DIR="
  if not exist "%PROJECT_DIR%\build-tools\check-minecraft-config.ps1" set "PROJECT_DIR="
)

if not defined PROJECT_DIR (
  set "SEARCH_DIR=%~dp0"
  call :find_project
)
if not defined PROJECT_DIR (
  set "SEARCH_DIR=%CD%"
  call :find_project
)
if not defined PROJECT_DIR (
  echo [ERROR] Could not locate the GIF Icon World project folder.
  echo Move this file inside the project folder, or set GIF_ICON_WORLD_PROJECT to the project path.
  pause
  exit /b 1
)

cd /d "%PROJECT_DIR%"
if errorlevel 1 goto :fatal_no_log

set "GRADLE_VERSION=9.8.2"
set "TOOLS_DIR=%LOCALAPPDATA%\GifIconWorldBuild"
set "GRADLE_HOME=%TOOLS_DIR%\gradle-%GRADLE_VERSION%"
set "JDK_CACHE=%TOOLS_DIR%\jdk-25"
set "BUILD_LOG=%PROJECT_DIR%\build-helper.log"
set "GRADLE_LOG=%PROJECT_DIR%\gradle-build.log"
set "DETECT_FILE=%TEMP%\gif_icon_world_jdk_detect.txt"
set "INSTALL_FILE=%TEMP%\gif_icon_world_jdk_install_path.txt"

>"%BUILD_LOG%" echo GIF Icon World build log
>>"%BUILD_LOG%" echo Project: %PROJECT_DIR%
>>"%BUILD_LOG%" echo Started: %DATE% %TIME%

call :say "============================================================"
call :say " GIF Icon World - build helper"
call :say " Project: %PROJECT_DIR%"
call :say " Log: %BUILD_LOG%"
call :say "============================================================"

where powershell.exe >nul 2>nul
if errorlevel 1 (
  call :say "[ERROR] Windows PowerShell 5.1 is required."
  goto :fatal
)

call :say "[0/3] Checking the Minecraft 26.3 build configuration..."
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%PROJECT_DIR%\build-tools\check-minecraft-config.ps1" >>"%BUILD_LOG%" 2>&1
if errorlevel 1 (
  powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%PROJECT_DIR%\build-tools\check-minecraft-config.ps1"
  goto :fatal
)
call :say "[OK] Fabric Loom configuration is ready."

if not exist "%TOOLS_DIR%" mkdir "%TOOLS_DIR%" >>"%BUILD_LOG%" 2>&1

call :say "[1/3] Looking for Java 25 or newer..."
set "DETECTED_JDK="

if exist "%JDK_CACHE%\bin\java.exe" if exist "%JDK_CACHE%\bin\javac.exe" set "DETECTED_JDK=%JDK_CACHE%"
if defined DETECTED_JDK goto :check_candidate

for /d %%D in ("%ProgramFiles%\Eclipse Adoptium\jdk-25*") do (
  if exist "%%~fD\bin\java.exe" if exist "%%~fD\bin\javac.exe" set "DETECTED_JDK=%%~fD"
)
if defined DETECTED_JDK goto :check_candidate

if exist "%DETECT_FILE%" del "%DETECT_FILE%" >nul 2>nul
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%PROJECT_DIR%\build-tools\find-jdk25.ps1" 1>"%DETECT_FILE%" 2>>"%BUILD_LOG%"
if not errorlevel 1 if exist "%DETECT_FILE%" set /p "DETECTED_JDK="<"%DETECT_FILE%"
if exist "%DETECT_FILE%" del "%DETECT_FILE%" >nul 2>nul
if defined DETECTED_JDK goto :check_candidate

call :say "[INFO] No suitable JDK was found. Preparing Temurin 25..."
goto :download_jdk

:check_candidate
set "JAVA_HOME=%DETECTED_JDK%"
call :say "[INFO] Checking: %JAVA_HOME%"
call :verify_jdk
if not errorlevel 1 goto :java_ready
call :say "[WARNING] The candidate did not pass validation. Preparing a clean JDK copy."
goto :download_jdk

:download_jdk
if exist "%INSTALL_FILE%" del "%INSTALL_FILE%" >nul 2>nul
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%PROJECT_DIR%\build-tools\install-jdk25.ps1" 1>"%INSTALL_FILE%" 2>>"%BUILD_LOG%"
if errorlevel 1 goto :jdk_download_failed
set "DETECTED_JDK="
if exist "%INSTALL_FILE%" set /p "DETECTED_JDK="<"%INSTALL_FILE%"
if exist "%INSTALL_FILE%" del "%INSTALL_FILE%" >nul 2>nul
if not defined DETECTED_JDK goto :jdk_download_failed
set "JAVA_HOME=%DETECTED_JDK%"
call :verify_jdk
if errorlevel 1 goto :jdk_download_failed
goto :java_ready

:verify_jdk
if not defined JAVA_HOME exit /b 1
if not exist "%JAVA_HOME%\bin\java.exe" exit /b 1
if not exist "%JAVA_HOME%\bin\javac.exe" exit /b 1
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "JAVA_MAJOR="
for /f "usebackq delims=" %%V in (`powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%PROJECT_DIR%\build-tools\get-java-major.ps1" -JavaExe "%JAVA_HOME%\bin\java.exe" 2^>nul`) do set "JAVA_MAJOR=%%V"
if not defined JAVA_MAJOR exit /b 1
if %JAVA_MAJOR% LSS 25 exit /b 1
>>"%BUILD_LOG%" echo Verified Java major: %JAVA_MAJOR%
>>"%BUILD_LOG%" echo JAVA_HOME: %JAVA_HOME%
"%JAVA_HOME%\bin\javac.exe" -version >>"%BUILD_LOG%" 2>&1
if errorlevel 1 exit /b 1
exit /b 0

:java_ready
call :say "[OK] Java %JAVA_MAJOR% is ready."
call :say "[2/3] Checking Gradle %GRADLE_VERSION%..."
if exist "%GRADLE_HOME%\bin\gradle.bat" goto :gradle_ready
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%PROJECT_DIR%\build-tools\install-gradle.ps1" -Version "%GRADLE_VERSION%" -InstallRoot "%TOOLS_DIR%" >>"%BUILD_LOG%" 2>&1
if errorlevel 1 goto :gradle_download_failed
if not exist "%GRADLE_HOME%\bin\gradle.bat" goto :gradle_download_failed

:gradle_ready
call :say "[OK] Gradle %GRADLE_VERSION% is ready."
call :say "[3/3] Running clean build..."
if exist "%GRADLE_LOG%" del "%GRADLE_LOG%" >nul 2>nul
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%PROJECT_DIR%\build-tools\run-gradle.ps1" -GradleBat "%GRADLE_HOME%\bin\gradle.bat" -ProjectDir "%PROJECT_DIR%" -LogFile "%GRADLE_LOG%"
set "BUILD_EXIT=%ERRORLEVEL%"
if not "%BUILD_EXIT%"=="0" goto :build_failed

call :say ""
call :say "[SUCCESS] Build completed."
call :say "Output: %PROJECT_DIR%\build\libs"
if exist "%PROJECT_DIR%\build\libs" explorer "%PROJECT_DIR%\build\libs"
call :say "Press any key to close this window."
pause >nul
exit /b 0

:jdk_download_failed
call :say "[ERROR] Automatic JDK setup failed. See build-helper.log."
if exist "%INSTALL_FILE%" type "%INSTALL_FILE%"
goto :fatal

:gradle_download_failed
call :say "[ERROR] Gradle setup failed. See build-helper.log."
goto :fatal

:build_failed
call :say "[BUILD FAILED] Gradle returned exit code %BUILD_EXIT%."
call :say "Full output: %GRADLE_LOG%"
goto :fatal

:fatal
call :say ""
call :say "The build stopped. This window is intentionally kept open."
call :say "Press any key to close."
pause >nul
exit /b 1

:fatal_no_log
echo [ERROR] Could not enter the project directory.
pause
exit /b 1

:say
echo(%~1
>>"%BUILD_LOG%" echo(%~1
exit /b 0

:find_project
if exist "%SEARCH_DIR%\build.gradle" if exist "%SEARCH_DIR%\gradle.properties" if exist "%SEARCH_DIR%\build-tools\check-minecraft-config.ps1" (
  set "PROJECT_DIR=%SEARCH_DIR%"
  exit /b 0
)
for %%P in ("%SEARCH_DIR%\..") do set "PARENT_DIR=%%~fP"
if /i "%PARENT_DIR%"=="%SEARCH_DIR%" exit /b 1
set "SEARCH_DIR=%PARENT_DIR%"
goto :find_project
