package it.unibs.ingsoft.v1.application;

import it.unibs.ingsoft.v1.domain.Configuratore;
import it.unibs.ingsoft.v1.domain.Credenziali;
import it.unibs.ingsoft.v1.persistence.api.ICredenzialiRepository;

import java.util.Objects;
import java.util.Optional;

/**
 * Handles configurator authentication and registration.
 *
 * <p>First access uses shared default credentials (config/config).
 * After login with those credentials, the controller forces the user to choose
 * personal credentials before allowing any operation.</p>
 */
public final class AuthenticationService {
    public static final String USERNAME_PREDEFINITO = "config";
    public static final String PASSWORD_PREDEFINITA = "config";

    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 4;

    private final ICredenzialiRepository repo;

    private Credenziali credenziali() {
        return repo.get();
    }

    /**
     * @pre repo   != null
     */
    public AuthenticationService(ICredenzialiRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    /**
     * Attempts to log in with the supplied credentials.
     *
     * @return the logged-in configurator, or empty if credentials are invalid
     */
    public Optional<Configuratore> login(String username, String password) {
        if (username == null || password == null)
            return Optional.empty();

        // Shared predefined credentials remain available for first access flows.
        if (USERNAME_PREDEFINITO.equals(username) &&
                PASSWORD_PREDEFINITA.equals(password))
            return Optional.of(new Configuratore(USERNAME_PREDEFINITO));

        String key = username.trim().toLowerCase();
        String stored = credenziali().getConfiguratori().get(key);
        if (stored != null && stored.equals(password))
            return Optional.of(new Configuratore(username));

        return Optional.empty();
    }

    /**
     * Registers a new configurator with personal credentials.
     *
     * @throws IllegalArgumentException if credentials are reserved, duplicate, or too short
     */
    public Configuratore registraNuovoConfiguratore(String username, String password) {
        validaNuovoAccount(username, password);

        String normalized = username.trim();
        credenziali().addConfiguratore(normalized, password);
        repo.save();
        return new Configuratore(normalized);
    }

    private void validaNuovoAccount(String username, String password) {
        validaCredenziali(username, password);

        if (USERNAME_PREDEFINITO.equalsIgnoreCase(username))
            throw new IllegalArgumentException("Lo username \"" + username + "\" è riservato.");

        if (esisteUsername(username))
            throw new IllegalArgumentException("Esiste già un utente (configuratore o fruitore) con username \"" + username + "\".");
    }

    /**
     * Returns true if an account with this username is already registered (either role).
     */
    public boolean esisteUsername(String username) {
        if (username == null) return false;
        String u = username.trim().toLowerCase();
        return credenziali().getConfiguratori().containsKey(u);
    }

    private static void validaCredenziali(String username, String password) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username non valido.");

        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password non valida.");

        if (username.length() < MIN_USERNAME_LENGTH)
            throw new IllegalArgumentException(
                    "Lo username deve avere almeno " + MIN_USERNAME_LENGTH + " caratteri.");
        if (password.length() < MIN_PASSWORD_LENGTH)
            throw new IllegalArgumentException(
                    "La password deve avere almeno " + MIN_PASSWORD_LENGTH + " caratteri.");
    }
}
