package hr.span.akademija.medtermin.sigurnost;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * S04 – CORS reflektira bilo koji Origin uz credentials (CWE-942). Dopušteni su samo origini iz konfiguracije.
 * WSTG-CLNT-07 · ASVS v5.0.0-3.4.2 (L1).
 */
@Tag("sigurnost")
@DisplayName("S04 · CORS za nepoznati origin")
class S04CorsTest extends SigurnosniTestBaza {

    private static final String ZLO = "https://zlo.example";

    @Test
    @DisplayName("GET s Origin: https://zlo.example ne dobiva Access-Control-Allow-Origin")
    void zahtjevSNepoznatogOrigina() {
        String ana = prijava("ana", "Ana12345!");

        Odgovor odgovor = posalji("GET", "/api/ja", ana, null, null, "Origin", ZLO);

        assertThat(odgovor.zaglavlje("Access-Control-Allow-Origin")).isNotIn(ZLO, "*");
    }

    @Test
    @DisplayName("preflight s nepoznatog origina nije dopušten")
    void preflightSNepoznatogOrigina() {
        Odgovor odgovor = posalji("OPTIONS", "/api/ja", null, null, null,
                "Origin", ZLO, "Access-Control-Request-Method", "GET");

        assertThat(odgovor.zaglavlje("Access-Control-Allow-Origin")).isNotIn(ZLO, "*");
    }
}
