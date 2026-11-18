package hr.span.akademija.medtermin.platforma;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Tokeni prijave čuvaju se u memoriji (token → sesija).
 *
 * N-01: token istječe nakon neaktivnosti ({@code app.sesija.neaktivnost}) i najkasnije nakon najduljeg
 * trajanja ({@code app.sesija.najdulje}); odjava ga opoziva, a promjena lozinke opoziva sve tokene korisnika.
 */
@Component
public class TokenStore {

    private record Sesija(PrijavljeniKorisnik korisnik, Instant izdana, Instant zadnjaUporaba) {

        Sesija uporabljena(Instant sada) {
            return new Sesija(korisnik, izdana, sada);
        }
    }

    private final SecureRandom random = new SecureRandom();
    private final ConcurrentHashMap<String, Sesija> sesije = new ConcurrentHashMap<>();
    private final Duration neaktivnost;
    private final Duration najdulje;

    public TokenStore(@Value("${app.sesija.neaktivnost:PT30M}") String neaktivnost,
                      @Value("${app.sesija.najdulje:PT8H}") String najdulje) {
        this.neaktivnost = Duration.parse(neaktivnost);
        this.najdulje = Duration.parse(najdulje);
    }

    public String izdaj(PrijavljeniKorisnik korisnik) {
        Instant sada = Instant.now();
        sesije.entrySet().removeIf(e -> istekla(e.getValue(), sada));   // čišćenje isteklih
        byte[] bajtovi = new byte[32];
        random.nextBytes(bajtovi);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bajtovi);
        sesije.put(token, new Sesija(korisnik, sada, sada));
        return token;
    }

    /** Korisnik za valjan token, inače {@code null}; istekli token se briše. Upotreba produljuje neaktivnost. */
    public PrijavljeniKorisnik korisnik(String token) {
        if (token == null) {
            return null;
        }
        Instant sada = Instant.now();
        Sesija sesija = sesije.computeIfPresent(token, (t, s) -> istekla(s, sada) ? null : s.uporabljena(sada));
        return sesija == null ? null : sesija.korisnik();
    }

    /** Odjava: token odmah prestaje vrijediti. */
    public void opozovi(String token) {
        if (token != null) {
            sesije.remove(token);
        }
    }

    /** Promjena lozinke: prestaju vrijediti svi tokeni korisnika. */
    public void opozoviSve(String username) {
        sesije.values().removeIf(s -> s.korisnik().username().equals(username));
    }

    private boolean istekla(Sesija sesija, Instant sada) {
        return sesija.zadnjaUporaba().plus(neaktivnost).isBefore(sada)
                || sesija.izdana().plus(najdulje).isBefore(sada);
    }
}
