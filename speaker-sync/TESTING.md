# Testy — przekierowanie dźwięku z telefonu

## Przygotowanie

- Samsung, Android 10+
- WiFi wspólna (telefon + TV/głośniki DLNA)
- Plik MP3 na telefonie (aplikacja Muzyka)
- Sparowany głośnik Bluetooth

## Test 1 — Bluetooth (3 min)

| # | Akcja | Wynik |
|---|-------|-------|
| 1 | Filtr BT → zaznacz głośnik | Badge „Routing Bluetooth” |
| 2 | Utwórz grupę → Start | Komunikat o routing BT |
| 3 | Odtwórz MP3 w aplikacji Muzyka | Dźwięk z głośnika BT, nie z telefonu |
| 4 | 2 głośniki BT → Dual Audio w panelu Media | Dźwięk na obu |

## Test 2 — WiFi DLNA (5 min)

| # | Akcja | Wynik |
|---|-------|-------|
| 1 | Filtr WiFi → zaznacz TV DLNA | Badge „Strumień z telefonu” |
| 2 | Utwórz grupę → Start | Prośba o zgodę Android (MediaProjection) |
| 3 | Zaakceptuj zgodę | URL strumienia `http://192.168.x.x:8770/live.pcm` |
| 4 | Odtwórz MP3 na telefonie | TV gra dźwięk |
| 5 | Zatrzymaj | Strumień się kończy |

## Test 3 — walidacja

| # | Akcja | Wynik |
|---|-------|-------|
| 1 | Zaznacz BT + WiFi razem | Błąd: wybierz jeden typ |
| 2 | 3 głośniki BT | Błąd: max 2 |
| 3 | Start bez wybranej grupy | Brak akcji |

## Checklist

- [ ] BT routing działa z plikiem lokalnym
- [ ] DLNA stream działa z plikiem lokalnym
- [ ] Zatrzymanie kończy przekierowanie
- [ ] Brak integracji ze Spotify/YouTube (celowo)

## Uwaga

Nie testuj Spotify/YouTube — te aplikacje blokują przechwytywanie dźwięku w Androidzie. Używaj plików lokalnych, gier lub stron w przeglądarce.
