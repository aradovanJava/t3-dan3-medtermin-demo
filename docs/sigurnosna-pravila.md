# Sigurnosna pravila i dokumentirane odluke – MedTermin

> Više ASVS zahtjeva traži da je pravilo **zapisano** (npr. 2.1.1, 6.1.1, 7.1.1, 8.1.1, 15.1.1, 16.1.1).
> Ovo je to mjesto – [asvs.md](asvs.md) se na njega poziva. Stanje: 18. 11. 2026.

## Pristup (ASVS 8.1.1)

| Funkcija | Anonimni | KORISNIK | LIJECNIK | ADMIN |
|---|---|---|---|---|
| prijava, registracija, reset lozinke | ✅ | ✅ | ✅ | ✅ |
| popis liječnika i termina | – | ✅ | ✅ | ✅ |
| nova rezervacija | – | ✅ (za sebe) | ✅ (za sebe) | ✅ |
| popis, ispis i otkazivanje rezervacije | – | samo svoje | samo svoje | sve |
| nalazi (popis, JSON/PDF, preuzimanje datoteke) | – | samo svoje | samo svoje | sve |
| uvoz termina (XML) | – | – | ✅ | ✅ |
| postavke obavijesti | – | svoje | svoje | svoje |

Pravila: **zadano odbijanje**; vlasništvo zapisa provjerava se u servisu prema prijavljenom korisniku, nikad prema
podatku iz zahtjeva; tuđi zapis vraća **404** (ne otkriva se da postoji). Dokazi: `S01TudaRezervacijaTest`,
`S13FailOpenTest`, `UvozTerminaFunkcionalniTest`, `NalaziFunkcionalniTest`.

## Provjera ulaza (ASVS 2.1.1)

| Ulaz | Pravilo | Greška |
|---|---|---|
| OIB | točno 11 znamenki | 400 |
| napomena | najviše 1000 znakova; kod ispisa HTML kodiranje | 400 |
| lozinka | najmanje 8 znakova, bez drugih pravila o sastavu | 400 |
| `sort` / `smjer` | samo `ime`, `prezime`, `specijalizacija` / `asc`, `desc` | 400 |
| `format` nalaza | samo `json`, `pdf` (vlasništvo se provjerava prije formata) | 400 / 404 |
| ime datoteke nalaza | mora postojati u bazi i pripadati korisniku; putanja ostaje u `storage-dir` | 400 / 404 |
| XML uvoza | DOCTYPE zabranjen | 400 |
| YAML postavki | samo osnovni tipovi (`SafeConstructor`) | 400 |
| `returnUrl` | samo lokalni put (`/…`, ne `//` ni `/\`) | preusmjerenje na `/` |

## Sesija

**Odluka (ASVS 7.1.1, 7.1.2):** token prijave (256 bita, `SecureRandom`) vrijedi do **30 minuta neaktivnosti** i
najdulje **8 sati** od prijave (jedna radna smjena u poliklinici). Odjava ga odmah opoziva; uspješna promjena
lozinke opoziva sve tokene korisnika. Broj istodobnih sesija po korisniku nije ograničen (pacijent i s mobitela i s
računala). Tokeni su u memoriji jedne instance – ponovno pokretanje poslužitelja odjavljuje sve korisnike; pri
skaliranju na više instanci treba zajedničko spremište. Uveo: popravak [N-01](nalazi/N-01-sesija-bez-isteka.md).

## Prijava

**Trenutno stanje (ASVS 6.1.1):** neuspjela prijava zapisuje se kao `WARN`; ograničenje broja pokušaja **ne
postoji** – nalaz [N-02](nalazi/N-02-bez-zastite-od-pogadanja-lozinke.md), rizik R-03. Planirano pravilo: nakon 5
neuspjelih pokušaja u 15 minuta za isti račun ili IP odgoda i `429`, bez trajnog zaključavanja računa.

## Rokovi za zakrpe ovisnosti (ASVS 15.1.1)

| Ozbiljnost (CVSS) | Rok za nadogradnju ili odluku |
|---|---|
| kritična (9,0–10) ili u CISA KEV-u | 7 dana |
| visoka (7,0–8,9) | 30 dana – sigurnosna vrata blokiraju |
| srednja (4,0–6,9) | 90 dana ili sljedeće redovno izdanje |
| niska | redovno održavanje |

Iznimka (`osv-scanner.toml`) je **privremena** odluka: svaka ima obrazloženje (koristi li aplikacija ranjivu
funkciju) i datum isteka; nakon isteka vrata ponovno padaju. Dependabot tjedno otvara PR-ove; vrata ih provjeravaju
kao i svaki drugi kod. Platforma izvan podrške (Spring Boot 3.5) je rizik R-02.

## Što se logira (ASVS 16.1.1)

| Događaj | Razina | Sadržaj |
|---|---|---|
| uspješna prijava | INFO | korisničko ime |
| neuspjela prijava | WARN | korisničko ime (nikad lozinka) |
| odjava | INFO | korisničko ime |
| registracija, promjena lozinke | INFO | korisničko ime |
| nova rezervacija | INFO | termin, korisnik, OIB maskiran (`*******6504`) |
| otkazivanje rezervacije | INFO | id rezervacije, korisnik |
| neočekivana greška | ERROR | tip greške (korisniku ide samo općenita poruka) |

Nikad u log: lozinke, tokeni prijave i reseta, puni OIB, sadržaj nalaza. Log je trenutno u konzoli poslužitelja –
centralizacija i zapis pristupa nalazima su rizik R-06.
