package hr.span.akademija.medtermin.sigurnost;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import hr.span.akademija.medtermin.ApiTestBaza;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Zajedničke pomoćne metode za sigurnosne testove.
 *
 * Svaki test stvara vlastiti termin (uvoz XML-a kao dr.horvat) i vlastite rezervacije, pa ne ovisi o
 * redoslijedu izvođenja ni o početnim podacima koje mijenjaju funkcionalni testovi.
 */
abstract class SigurnosniTestBaza extends ApiTestBaza {

    protected static final String OIB = "48291736504";

    /** Novi termin dr. Horvata sa zadanim kapacitetom; vraća id termina. */
    protected long noviTermin(int kapacitet) {
        String lijecnik = prijava("dr.horvat", "Ovlasteni12345!");
        String xml = "<termini><termin><lijecnikId>1</lijecnikId><pocetak>2027-03-01T08:00</pocetak>"
                + "<kapacitet>" + kapacitet + "</kapacitet><napomena>Termin za sigurnosni test</napomena>"
                + "</termin></termini>";
        Odgovor odgovor = posalji("POST", "/api/termini/uvoz", lijecnik, "application/xml", xml);
        assertThat(odgovor.status()).as("uvoz termina").isEqualTo(200);
        return odgovor.json().get("termini").get(0).get("id").asLong();
    }

    protected Odgovor rezerviraj(String token, long terminId, String napomena) {
        return postJson("/api/rezervacije", token, Map.of("terminId", terminId, "napomena", napomena, "oib", OIB));
    }

    /** Id-jevi rezervacija koje vidi vlasnik tokena ({@code GET /api/rezervacije/moje}). */
    protected List<Long> mojeRezervacije(String token) {
        List<Long> id = new ArrayList<>();
        for (JsonNode rezervacija : get("/api/rezervacije/moje", token).json()) {
            id.add(rezervacija.get("id").asLong());
        }
        return id;
    }

    /** Broj slobodnih mjesta u terminu prema {@code GET /api/termini}. */
    protected int slobodno(String token, long terminId) {
        for (JsonNode termin : get("/api/termini", token).json()) {
            if (termin.get("id").asLong() == terminId) {
                return termin.get("slobodno").asInt();
            }
        }
        throw new AssertionError("Termin " + terminId + " nije pronađen");
    }
}
