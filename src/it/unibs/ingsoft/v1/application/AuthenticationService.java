package it.unibs.ingsoft.v1.application;

import it.unibs.ingsoft.v1.domain.Configuratore;
import it.unibs.ingsoft.v1.persistence.api.IUtenteRepository;
import it.unibs.ingsoft.v1.persistence.dto.UtenteData;

import java.util.Objects;
import java.util.Optional;

public final class AuthenticationService
{
    public static final String USERNAME_PREDEFINITO = "config";
    public static final String PASSWORD_PREDEFINITA = "config";

    private final IUtenteRepository repo;
    private final UtenteData        utenti;

    /**
     * @pre repo   != null
     * @pre utenti != null
     */
    public AuthenticationService(IUtenteRepository repo, UtenteData utenti)
    {
        this.repo   = Objects.requireNonNull(repo);
        this.utenti = Objects.requireNonNull(utenti);
    }

    /**
     * Attempts to log in with the given credentials.
     *
     * @return a non-empty {@link Optional} containing the authenticated {@link Configuratore},
     *         or {@link Optional#empty()} if credentials do not match
     */
    public Optional<Configuratore> login(String username, String password)
    {
        if (username == null || password == null)
            return Optional.empty();

        String normalised = username.trim().toLowerCase();

        if (normalised.equals(USERNAME_PREDEFINITO) && password.equals(PASSWORD_PREDEFINITA))
            return Optional.of(new Configuratore(USERNAME_PREDEFINITO));

        String passSalvata = utenti.getConfiguratori().get(normalised);
        if (passSalvata != null && passSalvata.equals(password))
            return Optional.of(new Configuratore(normalised));

        return Optional.empty();
    }

    /**
     * Registers a new configurator with the given personal credentials.
     *
     * @throws IllegalArgumentException if credentials are too short, reserved, or already taken
     */
    public Configuratore registraNuovoConfiguratore(String newUsername, String newPassword)
    {
        validaCredenziali(newUsername, newPassword);

        String normalised = newUsername.trim().toLowerCase();

        if (USERNAME_PREDEFINITO.equalsIgnoreCase(normalised))
            throw new IllegalArgumentException("Username non consentito (riservato).");

        if (utenti.getConfiguratori().containsKey(normalised))
            throw new IllegalArgumentException("Username già esistente.");

        utenti.addConfiguratore(normalised, newPassword);
        repo.save(utenti);

        return new Configuratore(normalised);
    }

    public boolean esistonoConfiguratori()
    {
        return !utenti.getConfiguratori().isEmpty();
    }

    /** Returns {@code true} if the given username is already registered (UX guard for controllers). */
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

        if (username.trim().length() < 3)
            throw new IllegalArgumentException("Username troppo corto (min 3 caratteri).");

        if (password.trim().length() < 4)
            throw new IllegalArgumentException("Password troppo corta (min 4 caratteri).");
    }
}
