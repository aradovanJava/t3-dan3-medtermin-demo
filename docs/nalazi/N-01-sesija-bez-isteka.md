# N-01 · Ukradeni ili ostavljeni token prijave vrijedi zauvijek

| | |
|---|---|
| **Status** | ✅ popravljen 18. 11. 2026. ([PR #12](https://github.com/aradovanJava/t3-dan3-medtermin-demo/pull/12)) · ponovno ispitano |
| **Pronađeno** | ručnim ispitivanjem, RT-06 u [planu ispitivanja](../plan-ispitivanja.md) – nijedan alat iz pipelinea ga nije prijavio |
| **Mjesto** | `src/main/java/hr/span/akademija/medtermin/platforma/TokenStore.java` (tokeni bez vremena isteka), `AuthController` (nema odjave), `ResetLozinkeService` (promjena lozinke ne poništava tokene) |
| **CWE** | CWE-613 Insufficient Session Expiration |
| **WSTG v4.2** | WSTG-SESS-06 (odjava), WSTG-SESS-07 (istek sesije) |
| **ASVS 5.0.0** | v5.0.0-7.4.1 (L1) · v5.0.0-7.3.1, 7.3.2, 7.4.3 (L2) |
| **CVSS 4.0** | `CVSS:4.0/AV:N/AC:L/AT:P/PR:N/UI:N/VC:H/VI:L/VA:N/SC:N/SI:N/SA:N` → **8,3 (visoka)** |
| **Rizik** | [R-01](../registar-rizika.md#r-01): V 2 × U 3 = 6 → nakon popravka 3 |

## 1. Što i gdje

Token prijave (32 nasumična bajta – sam token je dobar) spremao se u mapu u memoriji **bez vremena izdavanja i bez
isteka**. Nije postojala odjava, a ni promjena lozinke nije poništavala postojeće tokene. Tko jednom dođe do tokena
– dijeljeno računalo u čekaonici, zaboravljena sesija, token u logu ili povijesti preglednika – čita nalaze i
mijenja rezervacije pacijenta sve dok se poslužitelj ne pokrene ponovno.

**Obrazloženje CVSS-a:** napad preko mreže (AV:N), bez posebne složenosti (AC:L), ali uz preduvjet da napadač
prvo dođe do tokena (AT:P); napadač nema vlastiti račun (PR:N), žrtva ne mora ništa napraviti (UI:N); čitanje
zdravstvenih podataka (VC:H), ograničena izmjena – otkazivanje i rezervacije (VI:L). Ocjena je izračunata
bibliotekom `cvss` i provjerljiva je u službenom FIRST kalkulatoru.

## 2. Dokaz (koraci za ponavljanje)

Tokeni i lozinke zamijenjeni su oznakama.

1. `POST /api/auth/login` kao novi korisnik → `200 {"token": "<T1>", …}`
2. `GET /api/ja` s `Authorization: Bearer <T1>` → `200`
3. Pričekati više od 30 minuta (ili zatražiti reset lozinke i postaviti novu lozinku tokenom iz e-pošte).
4. `GET /api/ja` s istim `<T1>` → **`200`** (očekivano: 401)
5. `POST /api/auth/odjava` s `<T1>` → **`404`** – odjava ne postoji.

Ponovljivo i automatski: test `N01SesijaTest` (4 testa) na kodu prije popravka **pada** – vidi crveno pokretanje
u [PR #12](https://github.com/aradovanJava/t3-dan3-medtermin-demo/pull/12).

## 3. Uzrok i popravak

**Uzrok:** `TokenStore` je čuvao samo par token → korisnik; nije bilo ni vremena ni načina za opoziv (ne
„nedostaje provjera“, nego „token nema životni vijek“).

**Popravak** ([PR #12](https://github.com/aradovanJava/t3-dan3-medtermin-demo/pull/12), dva commita – prvo test, zatim kod):

- token istječe nakon **30 minuta neaktivnosti** i najkasnije **8 sati** od prijave (`app.sesija.neaktivnost`,
  `app.sesija.najdulje`; odluka i obrazloženje u [sigurnosna-pravila.md](../sigurnosna-pravila.md#sesija));
- `POST /api/auth/odjava` opoziva token i briše kolačić `SESSION`;
- uspješna promjena lozinke resetom opoziva **sve** tokene tog korisnika.

**Sigurnosni test:** `src/test/java/hr/span/akademija/medtermin/sigurnost/N01SesijaTest.java` – crven na prvom
commitu PR-a, zelen nakon popravka.

## 4. Ponovno ispitivanje

Isti koraci 1–5 nakon popravka: korak 4 → `401`, korak 5 → `204`, a zatim `GET /api/ja` → `401`.
Ručno: RT-06 u [rucni-testovi.http](../rucni-testovi.http). **Status: popravljen.**
