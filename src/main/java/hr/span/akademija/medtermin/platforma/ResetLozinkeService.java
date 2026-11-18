package hr.span.akademija.medtermin.platforma;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Zaboravljena lozinka: token se šalje e-poštom, a njime se postavlja nova lozinka. */
@Service
public class ResetLozinkeService {

    private static final Duration TRAJANJE_TOKENA = Duration.ofMinutes(15);

    private final SecureRandom random = new SecureRandom();
    private final JdbcTemplate jdbc;
    private final KorisnikService korisnici;
    private final PostaService posta;

    public ResetLozinkeService(JdbcTemplate jdbc, KorisnikService korisnici, PostaService posta) {
        this.jdbc = jdbc;
        this.korisnici = korisnici;
        this.posta = posta;
    }

    public void zatrazi(String username) {
        Optional<KorisnikService.Korisnik> korisnik = korisnici.pronadi(username);
        if (korisnik.isEmpty()) {
            return;
        }
        byte[] bajtovi = new byte[32];
        random.nextBytes(bajtovi);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bajtovi);
        jdbc.update("INSERT INTO reset_tokeni (username, token, istjece) VALUES (?, ?, ?)",
                username, sazetak("SHA-256", token), LocalDateTime.now().plus(TRAJANJE_TOKENA));
        posta.posaljiUputeZaLozinku(korisnik.get(), token);
    }

    public boolean postaviNovuLozinku(String token, String novaLozinka) {
        KorisnikService.provjeriLozinku(novaLozinka);
        if (token == null || token.isBlank()) {
            return false;
        }
        List<Map<String, Object>> zapisi = jdbc.queryForList(
                "SELECT id, username FROM reset_tokeni WHERE token = ? AND iskoristen = FALSE AND istjece > ?",
                sazetak("SHA-256", token.trim()), LocalDateTime.now());
        if (zapisi.isEmpty()) {
            return false;
        }
        Map<String, Object> zapis = zapisi.get(0);
        long id = ((Number) zapis.get("id")).longValue();
        int iskoristeno = jdbc.update(
                "UPDATE reset_tokeni SET iskoristen = TRUE WHERE id = ? AND iskoristen = FALSE", id);
        if (iskoristeno != 1) {
            return false;
        }
        korisnici.promijeniLozinku((String) zapis.get("username"), novaLozinka);
        return true;
    }

    private static String sazetak(String algoritam, String vrijednost) {
        try {
            MessageDigest md = MessageDigest.getInstance(algoritam);
            return HexFormat.of().formatHex(md.digest(vrijednost.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(algoritam + " nije dostupan", e);
        }
    }
}
