@echo off
setlocal

echo ============================================
echo  SpeakerSync - budowa APK (debug)
echo ============================================
echo.

set "PROJECT_DIR=%~dp0.."
set "PROJECT_DIR=%PROJECT_DIR:~0,-1%"
cd /d "%PROJECT_DIR%"

if not exist "local.properties" (
    echo [BLAD] Brak pliku local.properties
    echo.
    echo Android Studio tworzy go automatycznie po otwarciu projektu.
    echo Albo skopiuj local.properties.example jako local.properties
    echo i ustaw sciezke sdk.dir do Android SDK.
    echo.
    pause
    exit /b 1
)

if not exist "gradlew.bat" (
    echo [BLAD] Brak gradlew.bat
    echo Otworz projekt w Android Studio i poczekaj na Gradle Sync.
    echo Android Studio wygeneruje Gradle Wrapper.
    exit /b 1
)

echo Buduje app-debug.apk ...
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo.
    echo [BLAD] Budowa nie powiodla sie.
    pause
    exit /b 1
)

echo.
echo ============================================
echo  GOTOWE
echo ============================================
echo APK:
echo   app\build\outputs\apk\debug\app-debug.apk
echo.
echo Zainstaluj na Samsung:
echo   1. Skopiuj APK na telefon i otworz
echo   2. LUB podlacz USB i uruchom: scripts\install-usb.bat
echo.
pause
