# Instalacja BEZ Android Studio

Nie masz Android Studio? Zainstaluj SpeakerSync na Samsungie przez GitHub (darmowe).

---

## Metoda 1 — GitHub (zalecana)

GitHub zbuduje APK w chmurze. Ty pobierzesz plik na telefon.

### Krok 1 — Konto GitHub

1. Wejdź na https://github.com
2. Załóż darmowe konto

### Krok 2 — Wrzuć projekt

1. GitHub → **New repository** → nazwa: `speaker-sync` → **Create**
2. **Upload files** → przeciągnij folder:
   `C:\Users\mymeg\Projects\speaker-sync`
3. **Commit changes**

### Krok 3 — Zbuduj APK

1. Zakładka **Actions**
2. **Build APK** → **Run workflow** → **Run workflow**
3. Poczekaj 5–10 min (zielony ptaszek)

### Krok 4 — Pobierz APK

1. Kliknij zakończony workflow
2. **Artifacts** → pobierz **SpeakerSync-apk** (ZIP)
3. Rozpakuj → plik **app-debug.apk**

### Krok 5 — Zainstaluj na Samsungu

1. Wyślij APK na telefon (WhatsApp, e-mail, Drive)
2. Otwórz w **Pliki**
3. Jeśli blokada: **Ustawienia → Bezpieczeństwo → Instalacja nieznanych aplikacji** → zezwól
4. **Zainstaluj** → otwórz **SpeakerSync**

---

## Metoda 2 — Ktoś z Android Studio

Wyślij folder projektu (ZIP) → osoba buduje APK → wysyła Ci `app-debug.apk` → instalujesz jak wyżej.

---

## Metoda 3 — Zainstaluj Android Studio

https://developer.android.com/studio — potem [INSTALACJA.md](INSTALACJA.md)

---

## Szybki test po instalacji

1. Sparuj głośnik BT
2. SpeakerSync → filtr **BT** → zaznacz głośnik → **Start**
3. Odtwórz MP3 w aplikacji Muzyka

Telefon: Android 10+
