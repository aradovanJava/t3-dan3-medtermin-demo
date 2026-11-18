package hr.span.akademija.medtermin.sigurnost;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

/**
 * S12 – logiranje (CWE-532, CWE-778). Alat ne čita log i ne zna da je 48291736504 OIB.
 * ASVS v5.0.0-16.2.5 (L2) osjetljivi podaci u logu · v5.0.0-16.3.1 (L2) zapis neuspjele prijave.
 */
@Tag("sigurnost")
@ExtendWith(OutputCaptureExtension.class)
@DisplayName("S12 · Što smije, a što ne smije u log")
class S12LogiranjeTest extends SigurnosniTestBaza {

    @Test
    @DisplayName("u logu nema lozinke ni punog OIB-a, a neuspjela prijava je zapisana kao WARN")
    void logBezTajniSaZapisomNeuspjelePrijave(CapturedOutput log) {
        String ana = prijava("ana", "Ana12345!");
        assertThat(rezerviraj(ana, noviTermin(1), "Pregled").status()).isEqualTo(201);
        Odgovor neuspjela = postJson("/api/auth/login", null, Map.of("username", "marko", "password", "pogresna"));
        assertThat(neuspjela.status()).isEqualTo(401);

        assertThat(log.getAll())
                .doesNotContain("Ana12345!")
                .doesNotContain(OIB)
                .contains("*******6504");
        assertThat(log.getAll().lines())
                .as("zapis neuspjele prijave za marka na razini WARN")
                .anyMatch(redak -> redak.contains("WARN") && redak.contains("marko"));
    }
}
