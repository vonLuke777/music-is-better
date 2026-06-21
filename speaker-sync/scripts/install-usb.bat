@echo off
setlocal

echo ============================================
echo  SpeakerSync - instalacja przez USB (adb)
echo ============================================
echo.

set "SDK=%LOCALAPPDATA%\Android\Sdk"
set "ADB=%SDK%\platform-tools\adb.exe"
set "APK=%~dp0..\app\build\outputs\apk\debug\app-debug.apk"

if not exist "%ADB%" (
    echo [BLAD] Nie znaleziono adb:
    echo   %ADB%
    echo Zainstaluj Android Studio i Android SDK Platform-Tools.
    pause
    exit /b 1
)

if not exist "%APK%" (
    echo [BLAD] Brak APK. Najpierw uruchom scripts\build-apk.bat
    pause
    exit /b 1
)

echo Sprawdzam podlaczone urzadzenia...
"%ADB%" devices
echo.

echo Instaluje SpeakerSync...
"%ADB%" install -r "%APK%"
if errorlevel 1 (
    echo.
    echo [BLAD] Instalacja nie powiodla sie.
    echo Sprawdz: debugowanie USB, zgoda na telefonie, sterownik Samsung.
    pause
    exit /b 1
)

echo.
echo ============================================
echo  Zainstalowano pomyslnie
echo ============================================
echo Na telefonie Samsung otworz aplikacje: SpeakerSync
echo.
pause
