package hr.span.akademija.medtermin.sigurnost;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * S10 – utrka kod zadnjeg slobodnog mjesta (CWE-362). Pojedinačni zahtjev je uvijek ispravan; greška postoji
 * samo kad stigne više zahtjeva istodobno – zato ju nijedan alat iz pipelinea ne vidi.
 * WSTG-BUSL-05 · ASVS v5.0.0-2.3.4 (L2).
 *
 * Bitno: stvarni poslužitelj na nasumičnom portu (ApiTestBaza, RANDOM_PORT – MockMvc ne pomaže), barijera da svi
 * zahtjevi krenu zajedno i provjera da je TOČNO jedan uspio.
 */
@Tag("sigurnost")
@DisplayName("S10 · Istodobna rezervacija zadnjeg mjesta")
class S10ZadnjeMjestoTest extends SigurnosniTestBaza {

    private static final int KORISNIKA = 10;

    @Test
    @DisplayName("10 istodobnih zahtjeva za termin s jednim mjestom: točno jedan 201, ostali 409")
    void samoJedanDobivaZadnjeMjesto() throws Exception {
        long termin = noviTermin(1);
        List<String> tokeni = new ArrayList<>();
        for (int i = 0; i < KORISNIKA; i++) {
            tokeni.add(prijava(noviKorisnik("utrka", "Lozinka12345!"), "Lozinka12345!"));
        }
        CyclicBarrier start = new CyclicBarrier(KORISNIKA);

        List<Integer> kodovi = new ArrayList<>();
        ExecutorService dretve = Executors.newFixedThreadPool(KORISNIKA);
        try {
            List<Future<Integer>> rezultati = new ArrayList<>();
            for (String token : tokeni) {
                rezultati.add(dretve.submit(() -> {
                    start.await();                       // svi kreću zajedno
                    return rezerviraj(token, termin, "Utrka").status();
                }));
            }
            for (Future<Integer> rezultat : rezultati) {
                kodovi.add(rezultat.get());
            }
        } finally {
            dretve.shutdownNow();
        }

        assertThat(kodovi).as("kodovi odgovora: %s", kodovi).containsOnly(201, 409);
        assertThat(kodovi.stream().filter(k -> k == 201).count()).as("uspješnih rezervacija").isEqualTo(1);
        assertThat(slobodno(tokeni.get(0), termin)).as("slobodnih mjesta nakon utrke").isZero();
    }
}
