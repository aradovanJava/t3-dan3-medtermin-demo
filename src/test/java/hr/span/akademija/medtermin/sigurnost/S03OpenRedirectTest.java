package hr.span.akademija.medtermin.sigurnost;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * S03 – open redirect nakon prijave (CWE-601). Dopušten je samo lokalni put.
 * ASVS v5.0.0-1.2.2 (L1).
 */
@Tag("sigurnost")
@DisplayName("S03 · Preusmjeravanje nakon prijave")
class S03OpenRedirectTest extends SigurnosniTestBaza {

    @ParameterizedTest(name = "returnUrl={0}")
    @ValueSource(strings = {"https://zlo.example/x", "//zlo.example", "/\\zlo.example"})
    @DisplayName("vanjska adresa u returnUrl ne preusmjerava izvan aplikacije")
    void nemaPreusmjerenjaVan(String returnUrl) {
        Odgovor odgovor = postForma("/prijava",
                Map.of("username", "ana", "password", "Ana12345!", "returnUrl", returnUrl));

        assertThat(odgovor.status()).isEqualTo(302);
        assertThat(odgovor.zaglavlje("Location")).doesNotContain("zlo.example");
    }
}
