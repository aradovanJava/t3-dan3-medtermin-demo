package hr.span.akademija.medtermin.sigurnost;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * N-01 (nalaz s Dana 3): token prijave nije istjecao i nije ga se moglo opozvati.
 * ASVS v5.0.0-7.4.1 (L1) prekid sesije · 7.3.1 / 7.3.2 (L2) neaktivnost i najdulje trajanje ·
 * 7.4.3 (L2) promjena lozinke prekida druge sesije. Vidi docs/nalazi/N-01-sesija-bez-isteka.md.
 *
 * Kratka trajanja i vlastita baza vrijede samo za ovaj test (zaseban Spring kontekst).
 */
@Tag("sigurnost")
@TestPropertySource(properties = {
        "app.sesija.neaktivnost=PT2S",
        "app.sesija.najdulje=PT5S",
        "spring.datasource.url=jdbc:h2:mem:sesija-n01;DB_CLOSE_DELAY=-1"})
@DisplayName("N-01 · Trajanje i prekid sesije")
class N01SesijaTest extends SigurnosniTestBaza {

    private static final String LOZINKA = "Sesija12345!";
    private static final Pattern TOKEN = Pattern.compile("Token:\\s*(\\S+)");

    private String noviToken() {
        return prijava(noviKorisnik("sesija", LOZINKA), LOZINKA);
    }

    @Test
    @DisplayName("nakon odjave token više ne vrijedi")
    void odjavaPonistavaToken() {
        String token = noviToken();
        assertThat(get("/api/ja", token).status()).isEqualTo(200);

        Odgovor odjava = posalji("POST", "/api/auth/odjava", token, null, null);

        assertThat(odjava.status()).isEqualTo(204);
        assertThat(get("/api/ja", token).status()).isEqualTo(401);
    }

    @Test
    @DisplayName("token istječe nakon neaktivnosti")
    void tokenIstjeceNakonNeaktivnosti() throws InterruptedException {
        String token = noviToken();
        assertThat(get("/api/ja", token).status()).isEqualTo(200);

        Thread.sleep(2_500);

        assertThat(get("/api/ja", token).status()).isEqualTo(401);
    }

    @Test
    @DisplayName("ni token u stalnoj upotrebi ne vrijedi dulje od najduljeg trajanja")
    void tokenImaNajduljeTrajanje() throws InterruptedException {
        String token = noviToken();
        for (int i = 0; i < 3; i++) {             // ~3 s: upotreba produljuje neaktivnost
            Thread.sleep(1_000);
            assertThat(get("/api/ja", token).status()).as("nakon %d s", i + 1).isEqualTo(200);
        }
        int zadnji = 0;
        for (int i = 0; i < 3; i++) {             // ~6 s: prešli smo najdulje trajanje (5 s)
            Thread.sleep(1_000);
            zadnji = get("/api/ja", token).status();
        }
        assertThat(zadnji).isEqualTo(401);
    }

    @Test
    @DisplayName("promjena lozinke resetom prekida postojeće sesije")
    void resetLozinkePrekidaSesije() throws IOException {
        String username = noviKorisnik("reset", LOZINKA);
        String stari = prijava(username, LOZINKA);
        assertThat(postJson("/api/auth/reset-zahtjev", null, Map.of("username", username)).status())
                .isEqualTo(202);

        Odgovor reset = postJson("/api/auth/reset", null,
                Map.of("token", tokenIzPoste(username), "novaLozinka", "NovaSesija12345!"));

        assertThat(reset.status()).isEqualTo(200);
        assertThat(get("/api/ja", stari).status()).isEqualTo(401);
    }

    private static String tokenIzPoste(String username) throws IOException {
        List<Path> poruke;
        try (Stream<Path> datoteke = Files.list(mapaPoste())) {
            poruke = datoteke.filter(p -> p.getFileName().toString().startsWith(username + "-")).toList();
        }
        assertThat(poruke).hasSize(1);
        Matcher m = TOKEN.matcher(Files.readString(poruke.get(0), StandardCharsets.UTF_8));
        assertThat(m.find()).isTrue();
        return m.group(1);
    }
}
