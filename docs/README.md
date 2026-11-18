# Vodič kroz demo – Dan 3 (SAMO ZA PREDAVAČA)

Demo prati prezentaciju **P3 – Ispitivanje sigurnosti, rizik i usklađenost**: za svaki blok je navedeno što otvoriti,
što reći i gdje je gotov primjer. Sve je na jednom projektu (MedTermin, Java), u stanju u kojem bi ga polaznik
predao u nedjelju. Repozitorij: <https://github.com/aradovanJava/t3-dan3-medtermin-demo> (privatan).

**Ne pokazivati:** izvorni kod u `src/main/` (referentno rješenje T2) i diff PR-a [PR #14](https://github.com/aradovanJava/t3-dan3-medtermin-demo/pull/14) (to je rješenje S10).
Pokazuju se Actions, Summary vrata, PR-ovi, testovi i dokumenti u `docs/`.

## Prije nastave (10 min)

1. Otvorite u pregledniku: **Actions**, **Pull requests** (i zatvorene) i ovu mapu `docs/`.
2. Lokalno pokrenite aplikaciju za blok 2: `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`;
   ako javlja grešku za skrivenu mapu `.m2`: `attrib -h "%USERPROFILE%\.m2"`). Port 8080 mora biti slobodan.
3. Otvorite [rucni-testovi.http](rucni-testovi.http) u IntelliJ-u ili VS Codeu (REST Client).
4. Po želji: [dfd.png](dfd.png) u zasebnoj kartici za blok 3.

## Blok 1 · Pipeline (slajdovi 5–7)

| Korak sa slajda 6 | Što pokazati | Gdje |
|---|---|---|
| 1 Predložak u repozitorij | prvi commit: T2 projekt + mapa `java/` iz paketa (s `.github` i `.semgrepignore`) | Commits → prvi commit |
| 2 Prvo pokretanje | zeleno, ali uz **30 prihvaćenih SCA nalaza** – opće iznimke `osv \| * \| org.apache.tomcat.embed:*` | [prvo pokretanje](https://github.com/aradovanJava/t3-dan3-medtermin-demo/actions/runs/44382947605) → Summary |
| 3 Ovisnosti | [PR #11](https://github.com/aradovanJava/t3-dan3-medtermin-demo/pull/11): bez općih iznimki → **13 blokirajućih** ([crveno](https://github.com/aradovanJava/t3-dan3-medtermin-demo/actions/runs/44384161534)); Spring Boot 3.5.16 + Tomcat 10.1.61 → ostalo 7 → privremene iznimke s rokom u `osv-scanner.toml` → [zeleno](https://github.com/aradovanJava/t3-dan3-medtermin-demo/actions/runs/44385250360) | PR, `osv-scanner.toml` |
| 3 Dependabot | 10 PR-ova odmah nakon prvog pusha, 4 ne prolaze vrata; [PR #4](https://github.com/aradovanJava/t3-dan3-medtermin-demo/pull/4) (Spring Boot 4.1.2) **ne prolazi** – velika verzija je migracija, ne klik | Pull requests |
| 4 Tajne i iznimke | `ci/prihvaceno.txt`: iznimke s **odsječkom** (S08), objašnjenje zašto je XXE iznimka široka i što je čuva | datoteka |
| 5 DAST s prijavom | `ci/dast-putanje.txt` – svi GET endpointi MedTermina | datoteka, artefakt `izvjestaj-dast` |
| 6 Dokaz pada | [PR #13](https://github.com/aradovanJava/t3-dan3-medtermin-demo/pull/13): vraćen S06 → **SCA i Semgrep** blokiraju; [PR #14](https://github.com/aradovanJava/t3-dan3-medtermin-demo/pull/14): vraćen S10 → **svi alati zeleni**, pada samo test (10 × 201 od 10 zahtjeva) | PR → Checks → Summary |

**Poruka:** [PR #14](https://github.com/aradovanJava/t3-dan3-medtermin-demo/pull/14) je most prema bloku 2 – propust koji nijedan alat ne vidi zaustavio je samo sigurnosni test.

Pitanja za polaznike: *Zašto su opće iznimke iz prvog pokretanja opasne?* (skrile bi i novu ranjivost u Tomcatu)
· *Što kad iznimci istekne rok?* (vrata ponovno padaju – odluka se mora obnoviti) · *Zašto Dependabot PR nije
automatski siguran?* (vrata ga provjeravaju kao svaki drugi kod; četiri od deset ne prolaze).

## Blok 2 · Ispitivanje (slajdovi 8–13)

1. **Plan** – [plan-ispitivanja.md](plan-ispitivanja.md): opseg, uloge (dva korisnika!), alati, sedam ručnih testova
   s WSTG oznakama i kriterij prolaza. Ovo je primjer „plana na jednoj stranici“.
2. **Što alati ne vide** (slajd 10) – u `src/test/java/hr/span/akademija/medtermin/sigurnost/` je po jedan test za
   S01, S09, S10, S12 i S13 (i S02–S04). Otvorite `S10ZadnjeMjestoTest`: stvarni poslužitelj (`ApiTestBaza`,
   `RANDOM_PORT`), `CyclicBarrier`, točno jedan 201 – Java verzija isječka sa slajda 11.
3. **Ručni test uživo** (slajd 11) – [rucni-testovi.http](rucni-testovi.http) na lokalnoj aplikaciji: prijava
   ana/marko, RT-01 (DELETE tuđe rezervacije → 404), RT-05 (`format=xml` → 400/404, bez sadržaja).
4. **Nalaz koji je ispitivanje stvarno našlo** – RT-06: token ne istječe i nema odjave.
   [N-01](nalazi/N-01-sesija-bez-isteka.md) pokazuje sva četiri dijela zapisa nalaza sa slajda 13 (što i gdje, dokaz,
   uzrok i popravak, ponovno ispitivanje) i CVSS 4.0 vektor (8,3).
5. **Crveno → zeleno** – [PR #12](https://github.com/aradovanJava/t3-dan3-medtermin-demo/pull/12): prvi commit samo test ([crveno](https://github.com/aradovanJava/t3-dan3-medtermin-demo/actions/runs/44386607639) – 4 od 4 testa padaju),
   drugi commit popravak ([zeleno](https://github.com/aradovanJava/t3-dan3-medtermin-demo/actions/runs/44386994602)). To je dokaz „test koji je bio crven, a sada je zelen“.
6. **Otvoren nalaz** – [N-02](nalazi/N-02-bez-zastite-od-pogadanja-lozinke.md) (nema ograničenja pokušaja prijave):
   nije popravljen, ide u registar rizika s vlasnikom i rokom.

## Blok 3 · Model prijetnji (slajdovi 14–17)

- [dfd.md](dfd.md) + [dfd.png](dfd.png): 7 vanjskih entiteta, 2 procesa, 5 spremišta, 14 tokova, 3 granice
  povjerenja. Pokažite F14 (artefakt iz CI-ja ulazi u poslužitelj) i D4 (sesije – tu je bio N-01).
- [stride.md](stride.md): 29 prijetnji, STRIDE po vrsti elementa; svaka ima kontrolu s dokazom (test, pravilo) ili
  ID rizika. Uključuje bazu (Dan 2) i CI/CD (Dan 1: akcije po oznaci, tj-actions).

## Blok 4 · Analiza rizika (slajdovi 18–21)

[registar-rizika.md](registar-rizika.md): 9 rizika na istim ljestvicama kao slajd 20, sva četiri postupanja
(izbjeći – R-04 seed računi, smanjiti – R-03, prenijeti – R-08 TLS kod pružatelja smještaja, prihvatiti – R-07),
vlasnik i rok za svaki. R-01 je zatvoren popravkom N-01 (6 → 3). R-02 povezuje blok 1 i 4: platforma izvan podrške,
iznimke s rokom i migracija koja ne prolazi vrata.

Pitanje: *Zašto R-04 (seed račun `admin`) ima postupanje „izbjeći“, a ne „smanjiti“?*

## Blok 5 · Usklađenost (slajdovi 22–26)

- [asvs.md](asvs.md): **svih 70 L1 zahtjeva** ASVS 5.0.0 (✅ 32 · ⚠️ 13 · ❌ 7 · ➖ 18 s razlogom) i 13 odabranih L2,
  svaki s dokazom. Pokažite da svaki ❌ vodi na rizik ili otvorenu stavku – to je razlika između liste i dokaza.
- [sigurnosna-pravila.md](sigurnosna-pravila.md): dokumentacijski ASVS zahtjevi (pristup, ulaz, sesija, rokovi za
  zakrpe, što se logira) na jednom mjestu.
- [uskladenost.md](uskladenost.md): četiri pitanja vodilje za polikliniku – posebna kategorija podataka (čl. 9),
  čl. 28 i 35, rokovi GDPR / NIS2 / CRA, pet pitanja za naručitelja.
- [SECURITY.md](../SECURITY.md): kanal za prijavu ranjivosti (CRA i ASVS traže da postoji).

## Što polaznik predaje (usporedba)

| Predaja (ZIP `Vxx-Ime-Prezime-T3.zip`) | U ovom demu |
|---|---|
| `repozitorij.txt` – repozitorij + crveno pokretanje | ovaj repozitorij + [PR #13](https://github.com/aradovanJava/t3-dan3-medtermin-demo/pull/13) / [PR #14](https://github.com/aradovanJava/t3-dan3-medtermin-demo/pull/14) |
| `usklađenost/` – ASVS lista, GDPR/NIS2 analiza | [asvs.md](asvs.md), [uskladenost.md](uskladenost.md) |
| `usklađenost/` – DFD sa STRIDE tablicom | [dfd.png](dfd.png), [stride.md](stride.md) |
| `usklađenost/` – registar od barem 5 rizika | [registar-rizika.md](registar-rizika.md) (9) |
| `baza/` – skripta i izlaz provjere (Dan 2) | nije dio ovog dema – vidi vodič za bazu (R-09) |

## Ponovno pokretanje dema

- Novo zeleno pokretanje: Actions → *devsecops* → **Run workflow** na `main`.
- Crvena pokretanja ostaju u PR-ovima [PR #13](https://github.com/aradovanJava/t3-dan3-medtermin-demo/pull/13), [PR #14](https://github.com/aradovanJava/t3-dan3-medtermin-demo/pull/14) i u prvom commitu [PR #12](https://github.com/aradovanJava/t3-dan3-medtermin-demo/pull/12).
- Dependabot tjedno otvara nove PR-ove – nakon nastave ih možete zatvoriti ili spojiti one koji prolaze.
- Provjera *Pregled ovisnosti u PR-u* u svakom PR-u je crvena: dependency review u privatnom repozitoriju
  traži GitHub Advanced Security. Posao ima `continue-on-error` i ne utječe na vrata – recite to prije nego što
  netko pita.
