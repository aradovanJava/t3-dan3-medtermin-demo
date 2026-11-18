# MedTermin – demo za Tjedan 3, Dan 3 (SAMO ZA PREDAVAČA)

Privatni demo repozitorij Secure Coding akademije: T2 projekt **MedTermin (Java, Spring Boot)** s DevSecOps
predloškom iz T3 paketa, na kojem je uživo prikazano svih pet blokova radionice *Ispitivanje sigurnosti, rizik i
usklađenost* (P3).

> Aplikacija je **referentno rješenje T2** (svih 13 propusta popravljeno). Na nastavi ne otvarajte izvorni kod u
> `src/main/` niti diff grane `dokaz/regresija-s10` – pokazujte Actions, Summary, PR-ove, testove i `docs/`.

**Vodič kroz demo po blokovima: [docs/README.md](docs/README.md)**

| Blok | Gdje |
|---|---|
| 1 · Pipeline | Actions → *devsecops*; PR-ovi „Ovisnosti“, „DOKAZ – ne spajati“ i Dependabot |
| 2 · Ispitivanje | [docs/plan-ispitivanja.md](docs/plan-ispitivanja.md), [docs/rucni-testovi.http](docs/rucni-testovi.http), [docs/nalazi/](docs/nalazi/), `src/test/java/…/sigurnost/` |
| 3 · Model prijetnji | [docs/dfd.md](docs/dfd.md), [docs/stride.md](docs/stride.md) |
| 4 · Analiza rizika | [docs/registar-rizika.md](docs/registar-rizika.md) |
| 5 · Usklađenost | [docs/asvs.md](docs/asvs.md), [docs/uskladenost.md](docs/uskladenost.md), [docs/sigurnosna-pravila.md](docs/sigurnosna-pravila.md), [SECURITY.md](SECURITY.md) |

Pokretanje aplikacije i testova: [KAKO-POKRENUTI.md](KAKO-POKRENUTI.md) (`./mvnw spring-boot:run`,
`./mvnw test -Dgroups=sigurnost`).
