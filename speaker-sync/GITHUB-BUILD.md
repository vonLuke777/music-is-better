# Instalacja APK — ostatnia poprawiona wersja

## Dlaczego wczesniej byly bledy?

Na GitHubie byl **stary kod** z 3 bledami kompilacji Kotlin. Sam workflow nie wystarczal.

Teraz skrypt `scripts/ci-prepare.sh` **naprawia wszystko automatycznie** przed budowa.

---

## Co musisz wrzucic na GitHub (2 pliki)

### 1. Workflow
Plik: `.github/workflows/build-apk.yml`

### 2. Skrypt naprawy
Plik: `scripts/ci-prepare.sh`

**Wazne:** oba pliki musza byc w repozytorium. Bez `ci-prepare.sh` budowa znowu sie wywali.

---

## Jak wrzucic pliki (najprosciej)

1. Otworz repo: https://github.com/vonLuke777/music-is-better
2. **Add file → Upload files**
3. Przeciagnij z komputera:
   - folder `.github` (z workflows/build-apk.yml)
   - folder `scripts` (z ci-prepare.sh)
4. **Commit changes**

Albo wrzuc **calym** folder projektu:
`C:\Users\mymeg\Projects\speaker-sync`

---

## Zbuduj APK

1. **Actions → Build APK → Run workflow**
2. Poczekaj na zielona fajke
3. **Releases** → pobierz **music-is-better.apk**
4. Wyslij na Samsung → zainstaluj

---

## Jesli nadal czerwony blad

Kliknij failed run → krok **Show build errors** → skopiuj log i wklej tutaj.
