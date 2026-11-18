package hr.span.akademija.medtermin.sigurnost;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * S01 – IDOR kod brisanja (CWE-639). Alat ne zna čiji je zapis, pa ovo provjeravamo testom s dva korisnika.
 * WSTG-ATHZ-04 · ASVS v5.0.0-8.2.2 (L1).
 */
@Tag("sigurnost")
@DisplayName("S01 · Brisanje tuđe rezervacije")
class S01TudaRezervacijaTest extends SigurnosniTestBaza {

    @Test
    @DisplayName("marko ne može obrisati aninu rezervaciju, a ona i dalje postoji")
    void markoNeMozeObrisatiAninuRezervaciju() {
        String ana = prijava("ana", "Ana12345!");
        String marko = prijava("marko", "Marko12345!");
        Odgovor nova = rezerviraj(ana, noviTermin(1), "Kontrolni pregled");
        assertThat(nova.status()).isEqualTo(201);
        long id = nova.json().get("id").asLong();

        Odgovor brisanje = delete("/api/rezervacije/" + id, marko);

        assertThat(brisanje.status()).as("tuđi zapis: 403 ili 404, nikako 204").isIn(403, 404);
        assertThat(mojeRezervacije(ana)).as("ana i dalje vidi svoju rezervaciju").contains(id);
    }

    @Test
    @DisplayName("vlasnik može otkazati svoju rezervaciju (kontrola ne smije blokirati ispravnu upotrebu)")
    void vlasnikMozeOtkazatiSvoju() {
        String ana = prijava("ana", "Ana12345!");
        long id = rezerviraj(ana, noviTermin(1), "Otkazujem").json().get("id").asLong();

        assertThat(delete("/api/rezervacije/" + id, ana).status()).isEqualTo(204);
        assertThat(mojeRezervacije(ana)).doesNotContain(id);
    }
}
