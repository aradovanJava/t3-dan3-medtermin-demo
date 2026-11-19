# Registar rizika – MedTermin

> Blok 4 radionice. Ulaz su prijetnje bez kontrole iz [stride.md](stride.md) i nalazi iz
> [nalazi/](nalazi/). Stanje: 18. 11. 2026.

## Ljestvice (dogovor tima, primjenjuju se jednako na sve rizike)

| Razina | Vjerojatnost (V) | Utjecaj (U) |
|---|---|---|
| 1 – niska | treba unutarnji pristup, povlašteni račun ili rijetke uvjete | zanemarivi podaci, bez obveze prijave |
| 2 – srednja | treba posebno znanje, pripremu ili prethodni korak (npr. curenje baze, ukradeni token) | osobni podaci manjeg broja korisnika, kraći prekid rada |
| 3 – visoka | dovoljan je običan korisnik (otvorena registracija), alat ili javni exploit | zdravstveni podaci, OIB; obveza prijave povrede ili incidenta |

**Rizik R = V × U:** 1–2 nizak (prihvatiti uz praćenje) · 3–4 srednji (smanjiti u dogovorenom roku) ·
6–9 visok (smanjiti prije isporuke). Postupanje: izbjeći · smanjiti · prenijeti · prihvatiti.
MedTermin obrađuje zdravstvene podatke (posebna kategorija, GDPR čl. 9), pa je utjecaj za sve što otkriva nalaze ili
rezervacije **3**.

## Registar

| ID | Rizik | V | U | R | Postupanje | Mjera i dokaz | R nakon | Vlasnik · rok | Status |
|---|---|---|---|---|---|---|---|---|---|
| [R-03](#r-03) | Pogađanje lozinke / credential stuffing ([N-02](nalazi/N-02-bez-zastite-od-pogadanja-lozinke.md)) | 3 | 3 | **9** | smanjiti | ograničenje pokušaja po računu i IP-u, provjera lozinke prema popisu čestih lozinki; test „6. pokušaj → 429“ | 3 | voditelj IT-a poliklinike · 15. 12. 2026. | otvoren |
| [R-04](#r-04) | Seed računi s poznatim lozinkama (`admin`, `dr.horvat`) u produkciji | 3 | 3 | **9** | izbjeći | seed samo u profilu `dev`; prvi administrator pri instalaciji; test „bez profila dev nema korisnika admin“ | 3 | voditelj razvoja · prije prve isporuke | otvoren – **blokira isporuku** |
| [R-01](#r-01) | Ukradeni ili ostavljeni token vrijedi zauvijek ([N-01](nalazi/N-01-sesija-bez-isteka.md)) | 2 | 3 | **6** | smanjiti | istek 30 min / 8 h, odjava, opoziv nakon promjene lozinke – `N01SesijaTest` ([PR #12](https://github.com/aradovanJava/t3-dan3-medtermin-demo/pull/12)) | 3 | voditelj IT-a poliklinike · 18. 11. 2026. | **zatvoren** |
| [R-02](#r-02) | Platforma Spring Boot 3.5 bez OSS podrške; ranjivosti Spring Frameworka 6.2 i Jacksona bez zakrpe | 2 | 3 | **6** | smanjiti | 3.5.16 + Tomcat 10.1.61 ([PR #11](https://github.com/aradovanJava/t3-dan3-medtermin-demo/pull/11)); iznimke s rokom u `osv-scanner.toml`; migracija na Spring Boot 4.1 ([PR #4](https://github.com/aradovanJava/t3-dan3-medtermin-demo/pull/4) – vrata ne prolaze) | 3 | voditelj razvoja · 31. 1. 2027. | u tijeku |
| [R-08](#r-08) | Zaštita prijenosa ovisi o okolini: TLS, HSTS, kolačić `Secure`, TLS prema BIS-u i bazi | 2 | 3 | **6** | prenijeti + smanjiti | ugovor s pružateljem smještaja (TLS 1.2+, HSTS, certifikat); kolačić `__Host-SESSION` sa `Secure` u aplikaciji | 3 | voditelj IT-a poliklinike · prije prve isporuke | otvoren |
| [R-09](#r-09) | Baza u produkciji bez mjera s Dana 2 (OIB u čistom tekstu, preširoke ovlasti, nešifrirane kopije) | 2 | 3 | **6** | smanjiti | skripta iz SQL Server laboratorija; dokaz: izlaz provjere 30/30 i demo „Upozorenja: 0“ | 3 | voditelj razvoja · prije prve isporuke | otvoren |
| [R-06](#r-06) | Log nije zaštićen ni centraliziran; nema zapisa tko je čitao nalaz | 2 | 2 | 4 | smanjiti | zapis pristupa nalazima (tko, koji nalaz, kada – bez sadržaja); log u središnji sustav samo za čitanje | 2 | voditelj razvoja · 31. 1. 2027. | otvoren |
| [R-05](#r-05) | CI/CD: akcije po pomičnoj oznaci (`@v5`), nema zaštite grane `main` | 1 | 3 | 3 | smanjiti, ostatak prihvatiti | akcije pribiti na SHA (Dependabot ih nastavlja ažurirati); zaštita grane kad repozitorij prijeđe na plan koji je podržava | 3 | voditelj razvoja · 30. 11. 2026. | otvoren |
| [R-07](#r-07) | Uskraćivanje usluge: nema ograničenja broja zahtjeva ni veličine tijela | 2 | 1 | 2 | prihvatiti | ograničenje na reverse proxyju, praćenje; ponovna procjena | 2 | voditelj IT-a poliklinike · procjena 1. 3. 2027. | prihvaćen |

**Kako čitati:** „R nakon“ je rizik uz uvedenu mjeru. Vjerojatnost pada, utjecaj ostaje – podaci su jednako
vrijedni. Ostane li 6 ili više, mjera nije dovoljna ili vlasnik rizika pisano prihvaća rizik s rokom.
**Prihvaćanje odobrava vlasnik rizika, ne programer.**

## Detalji

### R-01
**Ukradeni ili ostavljeni token vrijedi zauvijek.** Prijetnja: S (E1). Ranjivost: CWE-613 (nalaz N-01 iz ručnog
ispitivanja, WSTG-SESS-06/07). Posljedica: tko dođe do tokena (dijeljeno računalo, log, zaboravljena sesija), čita
nalaze i mijenja rezervacije pacijenta dok se poslužitelj ne pokrene ponovno. V = 2 (treba prvo doći do tokena),
U = 3 (zdravstveni podaci). **Zatvoreno:** [PR #12](https://github.com/aradovanJava/t3-dan3-medtermin-demo/pull/12), test `N01SesijaTest` bio je crven, a sada je zelen.

### R-02
**Platforma bez podrške.** Spring Boot 3.5 nema OSS izdanja od 30. 6. 2026. (3.5.16 je zadnje). Opće iznimke iz
predloška skrivale su 30 visokih nalaza; nadogradnja na 3.5.16 svela ih je na 13, a Tomcat 10.1.61 na 7. Za tih 7
(Spring Framework 6.2, Jackson) zakrpe unutar 3.5 nema. Za svaki je u `osv-scanner.toml` zapisano koristi li aplikacija ranjivu funkciju i rok
**31. 1. 2027.** – nakon roka vrata ponovno padaju. Pravo rješenje je migracija na Spring Boot 4.1: Dependabot je
otvorio [PR #4](https://github.com/aradovanJava/t3-dan3-medtermin-demo/pull/4), ali vrata na njemu ne prolaze (Spring Boot 4 donosi Jackson 3 s novim paketima `tools.jackson` – testovi se ne
prevode), pa je to zaseban posao.

### R-03
**Pogađanje lozinke.** Prijetnja: S (E1). Ranjivost: CWE-307 (nalaz N-02, WSTG-ATHN-03). Registracija je otvorena,
lozinka ima najmanje 8 znakova, a broj neuspjelih prijava nije ograničen. Privremeno: svaka neuspjela prijava
zapisuje se kao `WARN` (dokaz: `S12LogiranjeTest`) i prati. Mjera: nakon 5 neuspjelih pokušaja u 15 minuta
odgoda za taj račun i IP, odgovor 429; uz to ASVS 6.2.4 (popis čestih lozinki) i dokumentirana pravila (ASVS 6.1.1).

### R-04
**Seed računi u produkciji.** `PocetniPodaci` pri svakom pokretanju stvara korisnike s lozinkama iz
`KAKO-POKRENUTI.md` – uključujući administratora. U produkciji je to javno poznata administratorska lozinka
(ASVS 6.3.2). Postupanje je **izbjeći**: seed samo u profilu `dev`, prvi administrator pri instalaciji.

### R-05
**CI/CD.** Workflow ima `permissions: contents: read` (dobro), ali akcije su referencirane oznakom (`@v5`). Oznaku
autor – ili napadač s njegovim računom – može preusmjeriti na drugi commit (tj-actions/changed-files, ožujak 2025.,
CVE-2025-30066, u CISA KEV-u). Mjera: puni SHA commita u svakom `uses:`. Zaštita grane `main` za privatni
repozitorij ovisi o planu GitHub računa; dok je nema, vlasnik prihvaća preostali rizik.

### R-06
**Log i revizija.** Log je u konzoli poslužitelja: može se obrisati, nije centraliziran i ne bilježi tko je čitao
koji nalaz – za zdravstvene podatke to je pitanje odgovornosti (GDPR čl. 5(2)) i dokaza kod incidenta.

### R-07
**Uskraćivanje usluge.** Registracija (bcrypt), uvoz XML-a i parsiranje JSON-a nemaju ograničenja broja zahtjeva
ni veličine tijela. Utjecaj je kratki prekid rada (U = 1), pa vlasnik rizik **prihvaća** uz ograničenje na reverse
proxyju i praćenje, s ponovnom procjenom 1. 3. 2027.

### R-08
**Zaštita prijenosa.** TLS, HSTS i certifikat postavlja okolina (reverse proxy), ne aplikacija. Dio rizika se
**prenosi** ugovorom na pružatelja smještaja; u aplikaciji ostaje kolačić `SESSION` bez atributa `Secure` i
poziv BIS-u bez provjerenog TLS-a.

### R-09
**Baza.** Aplikacija u razvoju koristi H2. U produkciji je SQL Server, a mjere iz laboratorija s Dana 2 (minimalne
ovlasti, RLS, šifriranje OIB-a, TDE, šifrirane kopije) moraju biti primijenjene **prije** isporuke – dokaz je
izlaz automatske provjere (30/30 PROŠLO).
