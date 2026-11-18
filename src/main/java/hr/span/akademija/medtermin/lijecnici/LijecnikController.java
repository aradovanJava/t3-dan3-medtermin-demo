package hr.span.akademija.medtermin.lijecnici;

import hr.span.akademija.medtermin.platforma.PrijavljeniKorisnik;
import java.util.List;
import hr.span.akademija.medtermin.platforma.ApiException;
import java.util.Locale;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lijecnici")
public class LijecnikController {

    public record Lijecnik(long id, String ime, String prezime, String specijalizacija, String ordinacija) {
    }

    private static final RowMapper<Lijecnik> MAPPER = (rs, i) -> new Lijecnik(rs.getLong("id"),
            rs.getString("ime"), rs.getString("prezime"), rs.getString("specijalizacija"), rs.getString("ordinacija"));

    private static final Map<String, String> STUPCI = Map.of(
            "ime", "ime",
            "prezime", "prezime",
            "specijalizacija", "specijalizacija");

    private final JdbcTemplate jdbc;

    public LijecnikController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Popis liječnika, npr. {@code GET /api/lijecnici?sort=prezime&smjer=asc}. */
    @GetMapping
    public List<Lijecnik> popis(@RequestParam(defaultValue = "prezime") String sort,
                                @RequestParam(defaultValue = "asc") String smjer,
                                PrijavljeniKorisnik korisnik) {
        String stupac = STUPCI.get(sort);
        String redoslijed = switch (smjer.toLowerCase(Locale.ROOT)) {
            case "asc" -> "ASC";
            case "desc" -> "DESC";
            default -> null;
        };
        if (stupac == null || redoslijed == null) {
            throw ApiException.neispravno("Neispravan parametar sortiranja.");
        }
        String sql = "SELECT id, ime, prezime, specijalizacija, ordinacija FROM lijecnici ORDER BY "
                + stupac + " " + redoslijed + ", id";
        return jdbc.query(sql, MAPPER);
    }
}
