# Instalacja SpeakerSync na Samsung — pełna instrukcja

Projekt: `C:\Users\mymeg\Projects\speaker-sync`

Aplikacja przekierowuje **dźwięk z telefonu** na głośniki Bluetooth i WiFi (DLNA).  
Wymaga **Android 10+** (API 29).

---

## Część A — Przygotowanie komputera (jednorazowo)

### A1. Zainstaluj Android Studio

1. Pobierz: https://developer.android.com/studio  
2. Zainstaluj z domyślnymi opcjami  
3. Przy pierwszym uruchomieniu wybierz **Standard** setup  
4. Poczekaj na pobranie Android SDK

### A2. Zainstaluj sterownik Samsung USB

1. Pobierz: https://developer.samsung.com/android-usb-driver  
2. Zainstaluj sterownik  
3. Restart komputera (zalecane)

### A3. Sprawdź wymagania

| Element | Wersja |
|---------|--------|
| JDK | 17 (Android Studio ma wbudowany) |
| Android SDK | API 35 (compileSdk) |
| Min. telefon | Android 10 (API 29) |

---

## Część B — Otwarcie projektu

### B1. Otwórz projekt w Android Studio

1. Uruchom **Android Studio**  
2. **File → Open**  
3. Wybierz folder:  
   `C:\Users\mymeg\Projects\speaker-sync`  
4. Kliknij **OK**

### B2. Gradle Sync (pierwsze uruchomienie)

1. Android Studio pokaže „Gradle Sync” — **poczekaj** (5–15 minut przy pierwszym razie)  
2. Jeśli pojawi się błąd SDK:
   - **File → Settings → Languages & Frameworks → Android SDK**
   - Zaznacz: **Android 15 (API 35)**, **Android SDK Build-Tools**, **Platform-Tools**
   - Kliknij **Apply**
3. Android Studio automatycznie utworzy plik `local.properties` ze ścieżką SDK

> Jeśli `local.properties` nie powstał: skopiuj `local.properties.example` → `local.properties` i ustaw `sdk.dir`.

### B3. Sprawdź, czy projekt się kompiluje

W Android Studio:

1. **Build → Make Project** (Ctrl+F9)  
2. Na dole powinno być: **BUILD SUCCESSFUL**

Alternatywnie w terminalu (po Gradle Sync):

```bat
cd C:\Users\mymeg\Projects\speaker-sync
gradlew.bat assembleDebug
```

APK po budowie:  
`app\build\outputs\apk\debug\app-debug.apk`

---

## Część C — Przygotowanie telefonu Samsung

### C1. Włącz opcje programisty

1. **Ustawienia → Informacje o telefonie → Informacje o oprogramowaniu**  
2. Stuknij **Numer kompilacji** **7 razy**  
3. Pojawi się: „Jesteś teraz deweloperem”

### C2. Włącz debugowanie USB

1. **Ustawienia → Opcje programisty**  
2. Włącz **Debugowanie USB**  
3. (Opcjonalnie) **Pozostań aktywny** — przy testach  
4. (Opcjonalnie) **Wyłącz weryfikację aplikacji przez USB** — jeśli instalacja się nie udaje

### C3. Przygotuj głośniki do testu

**Bluetooth (test podstawowy):**

1. **Ustawienia → Połączenia → Bluetooth**  
2. Sparuj głośnik / słuchawki  
3. Upewnij się, że są **Połączone**

**WiFi / DLNA (test zaawansowany, opcjonalnie):**

1. Telefon i TV w **tej samej sieci WiFi** (nie „WiFi gość”)  
2. Na TV Samsung włącz udostępnianie DLNA / Smart View / AllShare  

**Plik testowy:**

- Skopiuj plik **MP3** na telefon (folder Muzyka / Pobrane)

---

## Część D — Instalacja aplikacji na telefon

Masz **3 metody**. Wybierz jedną.

---

### Metoda 1 — Android Studio + USB (ZALECANA)

1. Podłącz Samsung kablem USB-C do komputera  
2. Na telefonie wybierz tryb **Transfer plików / MTP**  
3. Na telefonie zaakceptuj: **Zezwolić na debugowanie USB?** → **Zezwól** (zaznacz „Zawsze”)  
4. W Android Studio u góry w liście urządzeń wybierz swój **Samsung** (nie emulator)  
5. Kliknij zielony **Run ▶** (Shift+F10)  
6. Aplikacja **SpeakerSync** zainstaluje się i uruchomi automatycznie

---

### Metoda 2 — Skrypt USB (APK + adb)

1. Zbuduj APK:
   ```bat
   cd C:\Users\mymeg\Projects\speaker-sync\scripts
   build-apk.bat
   ```
2. Podłącz telefon (debugowanie USB włączone)  
3. Zainstaluj:
   ```bat
   install-usb.bat
   ```

---

### Metoda 3 — APK bez kabla

1. W Android Studio: **Build → Build Bundle(s) / APK(s) → Build APK(s)**  
2. Otwórz folder z APK:
   `app\build\outputs\apk\debug\app-debug.apk`  
3. Wyślij plik na telefon (WhatsApp, e-mail, Google Drive)  
4. Na telefonie otwórz plik APK  
5. Jeśli system blokuje:
   - **Ustawienia → Bezpieczeństwo → Instalacja nieznanych aplikacji**
   - Zezwól dla aplikacji, z której otwierasz APK (Pliki / Chrome)  
6. Zainstaluj

---

## Część E — Pierwsze uruchomienie (uprawnienia)

Po otwarciu **SpeakerSync** zaakceptuj wszystkie prośby:

| Uprawnienie | Po co |
|-------------|-------|
| Bluetooth | Wykrywanie sparowanych głośników |
| Powiadomienia | Działanie w tle podczas przekierowania |
| Urządzenia w pobliżu / WiFi | Skanowanie sieci |
| Mikrofon / nagrywanie dźwięku | Przechwytywanie dźwięku z telefonu (WiFi/DLNA) |
| Nagrywanie ekranu (MediaProjection) | Wymóg Androida przy strumieniu WiFi |

Bez tych zgód aplikacja nie wykryje urządzeń lub nie przekieruje dźwięku.

---

## Część F — Test na Samsungu (krok po kroku)

### Test 1 — Bluetooth (3 minuty) ⭐ ZACZNIJ TUTAJ

1. Otwórz **SpeakerSync**  
2. Poczekaj 5–10 s — lista głośników  
3. Filtr **BT**  
4. Zaznacz checkbox przy swoim głośniku Bluetooth  
5. Dolny przycisk: **Utwórz grupę (1)**  
6. **Przekieruj dźwięk na Bluetooth**  
7. Ekran **Dźwięk z telefonu** → **Start — przekieruj dźwięk z telefonu**  
8. Otwórz aplikację **Muzyka** / **Pliki** na telefonie  
9. Odtwórz plik **MP3**

**Sukces:** dźwięk z głośnika BT, nie z telefonu.

**Dual Audio (2 głośniki BT):**

- Sparuj 2 urządzenia  
- Zaznacz oba w aplikacji (max 2)  
- Po starcie: **szybki panel → Media → zaznacz oba głośniki**

---

### Test 2 — WiFi / DLNA (opcjonalnie)

1. Filtr **WiFi**  
2. Zaznacz TV / urządzenie DLNA  
3. Utwórz grupę → **Przekieruj dźwięk na WiFi**  
4. **Start** → zaakceptuj zgodę Android (nagrywanie ekranu/dźwięku)  
5. Pojawi się URL: `http://192.168.x.x:8770/live.pcm`  
6. Odtwórz MP3 na telefonie  

**Sukces:** TV odtwarza dźwięk z telefonu.

**Zatrzymaj:** przycisk **Zatrzymaj** w aplikacji.

---

### Test 3 — Walidacja

| Akcja | Oczekiwany wynik |
|-------|------------------|
| Zaznacz BT + WiFi | Błąd — wybierz jeden typ |
| 3 głośniki BT | Błąd — max 2 |
| Odśwież listę (pull down) | Nowe urządzenia |

---

## Część G — Rozwiązywanie problemów

### Telefon nie widać w Android Studio

- [ ] Debugowanie USB włączone  
- [ ] Kabel obsługuje dane (nie tylko ładowanie)  
- [ ] Sterownik Samsung zainstalowany  
- [ ] Odłącz i podłącz USB, ponownie zezwól na debugowanie  
- [ ] W terminalu: `%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe devices`

### Gradle Sync failed

- [ ] Internet włączony  
- [ ] Android SDK API 35 zainstalowany  
- [ ] **File → Invalidate Caches → Restart**

### Pusta lista głośników

- [ ] Uprawnienia zaakceptowane  
- [ ] BT: głośnik sparowany w ustawieniach systemu  
- [ ] WiFi: ta sama sieć, odśwież listę  

### Brak dźwięku na BT

- [ ] Po „Start” odtwórz muzykę **na telefonie** (nie w SpeakerSync)  
- [ ] Głośność telefonu i głośnika w górę  
- [ ] W ustawieniach dźwięku wybierz głośnik BT  

### Brak dźwięku na TV (DLNA)

- [ ] Ta sama WiFi  
- [ ] Zaakceptowana zgoda MediaProjection  
- [ ] Telefon nie uśpiony  
- [ ] Użyj pliku MP3 (nie Spotify/YouTube — te blokują przechwytywanie)

### „App not installed” przy APK

- Odinstaluj starą wersję SpeakerSync  
- Włącz instalację z nieznanych źródeł  
- Upewnij się, że telefon ma Android 10+

---

## Część H — Checklist przed testem

```
[ ] Android Studio zainstalowane
[ ] Projekt otwarty, Gradle Sync OK
[ ] BUILD SUCCESSFUL
[ ] Samsung: opcje programisty + USB debugging
[ ] Głośnik BT sparowany
[ ] Plik MP3 na telefonie
[ ] Aplikacja zainstalowana (Run ▶ lub APK)
[ ] Wszystkie uprawnienia zaakceptowane
[ ] Test BT: MP3 gra z głośnika
```

---

## Szybka ścieżka (minimum)

```
Android Studio → Open speaker-sync → Gradle Sync
→ Podłącz Samsung USB → Run ▶
→ SpeakerSync → BT → zaznacz głośnik → Utwórz grupę
→ Start → odtwórz MP3 w Muzyce na telefonie
```

---

## Pliki pomocnicze w projekcie

| Plik | Opis |
|------|------|
| [README.md](README.md) | Opis aplikacji |
| [TESTING.md](TESTING.md) | Scenariusze testów |
| [local.properties.example](local.properties.example) | Szablon ścieżki SDK |
| [scripts/build-apk.bat](scripts/build-apk.bat) | Budowa APK |
| [scripts/install-usb.bat](scripts/install-usb.bat) | Instalacja przez adb |

---

## Odinstalowanie

**Ustawienia → Aplikacje → SpeakerSync → Odinstaluj**

lub przez adb:

```bat
adb uninstall pl.speakersync
```
