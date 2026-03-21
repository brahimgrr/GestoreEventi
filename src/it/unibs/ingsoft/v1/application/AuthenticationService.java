package it.unibs.ingsoft.v1.application;

import it.unibs.ingsoft.v1.domain.Configuratore;
import it.unibs.ingsoft.v1.persistence.api.IUtenteRepository;
import it.unibs.ingsoft.v1.persistence.dto.UsersData;

import java.util.Objects;
import java.util.Optional;

/**
 * Handles configurator authentication and registration.
 *
 * <p>First access uses default credentials (config/config).
 * After personal credentials are registered, the default account is disabled.</p>
 */
public final class AuthenticationService
{
    public static final String USERNAME_PREDEFINITO = "config";
    public static final String PASSWORD_PREDEFINITA = "config";

    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 4;

    private final IUtenteRepository repo;
    private final UsersData utenti;

    /**
     * @pre repo   != null
     * @pre utenti != null
     */
    public AuthenticationService(IUtenteRepository repo)
    {
        this.repo   = Objects.requireNonNull(repo);
        this.utenti = repo.load();
    }

    /**
     * Attempts to log in with the supplied credentials.
     *
     * @return the logged-in configurator, or empty if credentials are invalid
     */
    public Optional<Configuratore> login(String username, String password)
    {
        if (username == null || password == null)
            return Optional.empty();

        // Default credentials: valid only when no personal accounts exist yet
        if (USERNAME_PREDEFINITO.equals(username) &&
            PASSWORD_PREDEFINITA.equals(password) &&
            utenti.getConfiguratori().isEmpty())
            return Optional.of(new Configuratore(USERNAME_PREDEFINITO));

        String stored = utenti.getConfiguratori().get(username);
        if (stored != null && stored.equals(password))
            return Optional.of(new Configuratore(username));

        return Optional.empty();
    }

    /**
     * Registers a new configurator with personal credentials.
     *
     * @throws IllegalArgumentException if credentials are reserved, duplicate, or too short
     */
    public Configuratore registraNuovoConfiguratore(String username, String password)
    {
        validaCredenziali(username, password);

        if (USERNAME_PREDEFINITO.equalsIgnoreCase(username))
            throw new IllegalArgumentException("Lo username \"" + username + "\" è riservato.");

        if (utenti.getConfiguratori().containsKey(username))
            throw new IllegalArgumentException("Esiste già un configuratore con username \"" + username + "\".");

        utenti.addConfiguratore(username, password);
        repo.save(utenti);
        return new Configuratore(username);
    }

    /** Returns true if a configurator with this username is already registered. */
    public boolean esisteUsername(String username)
    {
        if (username == null) return false;
        return utenti.getConfiguratori().containsKey(username.trim().toLowerCase());
    }

    private static void validaCredenziali(String username, String password)
    {
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
