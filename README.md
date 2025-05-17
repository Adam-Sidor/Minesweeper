# Minesweeper

Moja implementacja klasycznej gry Saper z wykorzystaniem React (frontend) i Spring Boot (backend).

---

## Spis treści

- [Opis projektu](#opis-projektu)  
- [Funkcje](#funkcje)  
- [Technologie](#technologie)  
- [Instrukcja uruchomienia](#instrukcja-uruchomienia)  
- [Struktura projektu](#struktura-projektu)  
- [Możliwe usprawnienia](#możliwe-usprawnienia)  
- [Autor](#autor)

---

## Opis projektu

Minesweeper to klasyczna gra logiczna, w której celem jest odkrycie wszystkich pól nie zawierających min, bez ich detonacji. W mojej implementacji backend napisałem w Javie ze Spring Bootem, a frontend w React.

---

## Funkcje

- **Bezpieczny start:** Pierwsze kliknięcie odkrywa pole, które na pewno nie zawiera miny.  
- **Restart gry:** Możliwość szybkiego restartu za pomocą przycisku "R".  
- **Trzy poziomy trudności:** Łatwy, średni i trudny z różnymi rozmiarami planszy i liczbą min.  
- **Tryb niestandardowy:** Ustaw własne parametry planszy i liczby min.  
- **Globalna tablica wyników:** Wyniki przechowywane w SQLite i dostępne dla wszystkich graczy na danym poziomie trudności.  
- **Zapamiętywanie ustawień:** Wybrany poziom trudności jest zapisywany lokalnie i ładowany przy kolejnym uruchomieniu gry.

---

## Technologie

- **Frontend:** React, TypeScript, CSS  
- **Backend:** Spring Boot (Java), REST API  
- **Baza danych:** SQLite  

---

## Instrukcja uruchomienia

1. **Backend:**  
   - Przejdź do folderu `\backend`.  
   - Uruchom `mvn spring-boot:run` lub skompiluj i uruchom aplikację z IDE.  
   - Backend nasłuchuje pod adresem `http://localhost:8080`.  

2. **Frontend:**  
   - Przejdź do folderu `\frontend\minesweeper-frontend`.  
   - Zainstaluj zależności: `npm install` lub `yarn install`.  
   - Uruchom frontend: `npm start` lub `yarn start`.  
   - Aplikacja będzie dostępna pod `http://localhost:3000`.

---

## Struktura projektu

```
Minesweeper
├── backend # Backend w Spring Boot (Java)
├── frontend # Frontend w React (TypeScript)
└── README.md # Ten plik
```


---

## Możliwe usprawnienia

- Dodanie testów jednostkowych dla backendu i frontend.  
- Dodanie wsparcia dla urządzeń mobilnych.  

---

## Autor

- Adam Sidor  
- Kontakt: sidoadsi1@gmail.com  

---

Dziękuję za zainteresowanie projektem!
