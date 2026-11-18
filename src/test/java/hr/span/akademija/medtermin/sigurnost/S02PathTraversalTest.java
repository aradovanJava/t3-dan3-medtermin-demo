package hr.span.akademija.medtermin.sigurnost;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * S02 – path traversal kod preuzimanja datoteke (CWE-22). Kanarinac izvan mape s dokumentima ne smije se moći
 * pročitati, ni kad mu ime sadrži korisničko ime.
 * WSTG-ATHZ-01 · ASVS v5.0.0-5.3.2 (L1).
 */
@Tag("sigurnost")
@DisplayName("S02 · Preuzimanje datoteke izvan mape s dokumentima")
class S02PathTraversalTest extends SigurnosniTestBaza {

    @Test
    @DisplayName("../tajna-ana-… ne vraća sadržaj kanarinca")
    void kanarinacIzvanMapeNijeDostupan() throws IOException {
        String sadrzaj = "KANARINAC-" + UUID.randomUUID();
        String ime = "tajna-ana-" + UUID.randomUUID() + ".txt";
        Path kanarinac = mapaDokumenata().toAbsolutePath().getParent().resolve(ime);
        Files.writeString(kanarinac, sadrzaj, StandardCharsets.UTF_8);
        String ana = prijava("ana", "Ana12345!");

        Odgovor odgovor = get("/api/nalazi/preuzmi?datoteka=" + enc("../" + ime), ana);

        assertThat(odgovor.status()).isNotEqualTo(200);
        assertThat(odgovor.tijelo()).doesNotContain(sadrzaj);
    }
}
