# Model prijetnji – STRIDE po elementu

> Blok 3 radionice. Elementi i ID-jevi su iz [dfd.md](dfd.md). Pravilo: **za svaku prijetnju postojeća kontrola
> (s dokazom) ili ID rizika u [registru](registar-rizika.md)** – prijetnja bez odgovora je nalaz.

STRIDE po elementu (Microsoft SDL): vanjski entitet → S, R · proces → S, T, R, I, D, E · spremište → T, I, D
(+ R za log) · tok podataka → T, I, D.

| Element | STRIDE | Prijetnja | Postojeća kontrola (dokaz) | Rizik |
|---|---|---|---|---|
| E1 Pacijent | S | Pogađanje lozinke ili credential stuffing (otvorena registracija, nema ograničenja pokušaja) | bcrypt, najmanje 8 znakova, WARN zapis neuspjele prijave – `S12LogiranjeTest`; **nema ograničenja broja pokušaja** ([N-02](nalazi/N-02-bez-zastite-od-pogadanja-lozinke.md)) | [R-03](registar-rizika.md#r-03) |
| E1 Pacijent | S | Ukradeni ili ostavljeni token (dijeljeno računalo, log) koristi se danima | istek nakon 30 min neaktivnosti i najkasnije 8 h, odjava, opoziv nakon promjene lozinke – `N01SesijaTest` ([N-01](nalazi/N-01-sesija-bez-isteka.md)) | R-01 (zatvoren) |
| E1 Pacijent | R | „Nisam ja otkazao termin“ | zapis otkazivanja s korisnikom (`RezervacijaService.obrisi`) – log nije zaštićen | [R-06](registar-rizika.md#r-06) |
| E2 Liječnik | E | Pacijent poziva uvoz termina | uvoz samo za `LIJECNIK` i `ADMIN` – `UvozTerminaFunkcionalniTest` | – |
| E3 Administrator | S | Seed račun `admin` s poznatom lozinkom završi u produkciji | – (`PocetniPodaci` puni seed pri svakom pokretanju) | [R-04](registar-rizika.md#r-04) |
| P1 API | E | Brisanje tuđe rezervacije – IDOR (S01) | uvjet vlasnika u servisu – `S01TudaRezervacijaTest` | – |
| P1 API | E | Fail-open: nepoznat format propušta tuđi nalaz (S13) | vlasništvo se provjerava prije formata, 400/404 – `S13FailOpenTest` | – |
| P1 API | T | Utrka za zadnje slobodno mjesto (S10) | atomski uvjetni `UPDATE … AND zauzeto < kapacitet` – `S10ZadnjeMjestoTest` | – |
| P1 API | T, I | Pohranjeni XSS na potvrdi za ispis (S09) | HTML kodiranje + `Content-Security-Policy` – `S09IspisXssTest` | – |
| P1 API | I | Tekst iznimke ili stack trace u odgovoru | `GreskeHandler`, `server.error.include-stacktrace=never` – `S13FailOpenTest` provjerava tijelo | – |
| P1 API | T, E | XXE u uvozu termina (S05) | DOCTYPE zabranjen; vlastito Semgrep pravilo čuva regresiju (`ci/semgrep/regresije.yml`) | – |
| P1 API | T, E | Nesigurna deserijalizacija YAML-a (S06) | snakeyaml 2.x + `SafeConstructor`; SCA u vratima (dokaz pada: grana `dokaz/regresija-s06`) | – |
| P1 API | D | Mnogo zahtjeva, veliko tijelo zahtjeva, DoS u parsiranju JSON-a | – | [R-07](registar-rizika.md#r-07) |
| P1 platforma | T, I, D | Poznate ranjivosti platforme (Spring Boot 3.5 bez OSS podrške od 30. 6. 2026.) | OSV-Scanner u vratima, Dependabot, Spring Boot 3.5.16 + Tomcat 10.1.61, iznimke s rokom u `osv-scanner.toml` | [R-02](registar-rizika.md#r-02) |
| D1 Baza | T, I | SQL injection u sortiranju (S08) | mapa dopuštenih stupaca i smjerova; Semgrep + SpotBugs (uska iznimka s odsječkom) | – |
| D1 Baza | I | OIB u čistom tekstu, nešifrirane kopije baze | Dan 2: šifriranje stupca, TDE, šifrirane kopije – još nije primijenjeno u produkciji | [R-09](registar-rizika.md#r-09) |
| D1 Baza | E | Aplikacija se spaja kao `sa` ili `db_owner` | Dan 2: `uloga_aplikacija`, minimalne ovlasti, RLS – još nije primijenjeno u produkciji | [R-09](registar-rizika.md#r-09) |
| D2 Dokumenti nalaza | I | Path traversal kod preuzimanja (S02) | ime datoteke iz baze + normalizirana putanja – `S02PathTraversalTest` | – |
| D3 Log | I | Lozinka i OIB u logu (S12) | lozinka se ne logira, OIB maskiran – `S12LogiranjeTest` | – |
| D3 Log | R, T | Log se može izmijeniti ili obrisati; nema zapisa tko je čitao nalaz | – | [R-06](registar-rizika.md#r-06) |
| D4 Sesije | D | Ponovno pokretanje poslužitelja odjavljuje sve korisnike | odluka dizajna za jednu instancu ([sigurnosna-pravila.md](sigurnosna-pravila.md#sesija)); pri skaliranju zajedničko spremište | – |
| F1/F2 preglednik ↔ API | I | CORS reflektira bilo koji Origin (S04) | popis origina iz konfiguracije – `S04CorsTest`; ZAP kuka u DAST-u | – |
| F1/F2 preglednik ↔ API | I, T | Promet bez TLS-a; kolačić `SESSION` bez atributa `Secure` | TLS na reverse proxyju – izvan koda aplikacije | [R-08](registar-rizika.md#r-08) |
| F9 e-pošta | S | Predvidljiv ili višekratni token za reset lozinke (S11) | 32 bajta iz `SecureRandom`, u bazi samo hash, 15 min, jednokratan – `PromjenaLozinkeFunkcionalniTest` | – |
| F10 BIS | S, I | Lažni BIS ili presretanje poziva | – (u laboratoriju simulacija) | [R-08](registar-rizika.md#r-08) |
| P2 GitHub Actions | T | Akcija referencirana pomičnom oznakom (`@v5`) – slučaj tj-actions, CVE-2025-30066 | `permissions: contents: read`, Dependabot; **akcije nisu pribijene na SHA** | [R-05](registar-rizika.md#r-05) |
| P2 GitHub Actions | I | Tajne u logu pokretanja; `pull_request_target` s kodom iz PR-a | workflow ne koristi `pull_request_target`; tajna samo u poslu DAST; gitleaks na cijeloj povijesti | – |
| D5 Repozitorij | R, T | Force-push na `main`, izmjena bez pregleda | – (zaštita grane ovisi o planu GitHub računa) | [R-05](registar-rizika.md#r-05) |
| E7 Registar paketa | T | Izmišljen ili kompromitiran paket (slopsquatting, preuzeta izdanja) | SBOM + OSV-Scanner, Dependabot s odgodom od 3 dana, pregled PR-a | – |

**29 prijetnji · 17 s postojećom kontrolom (uključujući R-01, zatvoren popravkom N-01) · 12 vodi na 8 otvorenih
rizika u registru (R-02 – R-09).**

## Kako je tablica nastala

1. Za svaki element DFD-a postavljena su samo STRIDE pitanja koja se na njegovu vrstu odnose.
2. Za svaku prijetnju potražena je kontrola u kodu ili konfiguraciji i **dokaz** (test, pravilo alata, izvještaj).
   Tvrdnja „imamo validaciju“ bez dokaza nije kontrola.
3. Što nema kontrolu, otišlo je u registar rizika s vlasnikom i rokom.
4. Model se ponovno pregledava kad se promijeni arhitektura (npr. Keycloak, više instanci, nova integracija).
