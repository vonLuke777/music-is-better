# SpeakerSync

Przekierowuje dźwięk z telefonu Samsung na głośniki Bluetooth i WiFi (DLNA).

## Szybki start

**Nie masz Android Studio?**  
👉 **[INSTALACJA-BEZ-STUDIO.md](INSTALACJA-BEZ-STUDIO.md)** — budowa APK przez GitHub (darmowe)

**Masz Android Studio?**  
👉 **[INSTALACJA.md](INSTALACJA.md)** — instalacja przez USB

## Wymagania

- Android Studio + Samsung USB Driver
- Samsung z **Android 10+**
- Sparowany głośnik Bluetooth (test podstawowy)

## Budowa APK (Windows)

```bat
cd scripts
build-apk.bat
install-usb.bat
```

## Dokumentacja

| Plik | Zawartość |
|------|-----------|
| [INSTALACJA.md](INSTALACJA.md) | Instalacja krok po kroku |
| [TESTING.md](TESTING.md) | Scenariusze testów |

## Jak działa

| Głośnik | Jakość |
|---------|--------|
| Bluetooth | aptX / LDAC (system Android) |
| WiFi DLNA | PCM lossless 48 kHz |

Bez Spotify, YouTube i licencji Cast — odtwarzasz dźwięk normalnie na telefonie, aplikacja kieruje go na głośniki.
