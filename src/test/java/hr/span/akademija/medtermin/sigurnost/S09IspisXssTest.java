package hr.span.akademija.medtermin.sigurnost;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * S09 – pohranjeni XSS na stranici za ispis (CWE-79). Pasivni ZAP ne sprema unos i ne otvara ispis.
 * WSTG-INPV-02 · ASVS v5.0.0-1.2.1 (L1), v5.0.0-3.4.3 (L2, CSP).
 */
@Tag("sigurnost")
@DisplayName("S09 · Pohranjeni XSS na potvrdi za ispis")
class S09IspisXssTest extends SigurnosniTestBaza {

    private static final String NAPAD = "<script>alert('x')</script><img src=x onerror=alert(1)>";

    @Test
    @DisplayName("napomena s HTML-om ispisuje se kodirano, uz Content-Security-Policy")
    void napomenaSeIspisujeKodirano() {
        String ana = prijava("ana", "Ana12345!");
        Odgovor nova = rezerviraj(ana, noviTermin(1), NAPAD);
        assertThat(nova.status()).isEqualTo(201);

        Odgovor ispis = get("/ispis/rezervacije/" + nova.json().get("id").asLong(), ana);

        assertThat(ispis.status()).isEqualTo(200);
        assertThat(ispis.tijelo())
                .doesNotContain("<script>alert('x')</script>")
                .doesNotContain("<img src=x onerror")
                .contains("&lt;script&gt;");
        assertThat(ispis.zaglavlje("Content-Security-Policy")).contains("default-src 'none'");
    }
}
