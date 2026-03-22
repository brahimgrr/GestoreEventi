package it.unibs.ingsoft.v3.application;

import it.unibs.ingsoft.v3.domain.AppConstants;
import it.unibs.ingsoft.v3.domain.Configuratore;
import it.unibs.ingsoft.v3.domain.Fruitore;
import it.unibs.ingsoft.v3.domain.Credenziali;

import java.util.Objects;

public final class AuthenticationService
{
    /** @deprecated Use {@link it.unibs.ingsoft.v3.domain.AppConstants#USERNAME_PREDEFINITO} */
    public static final String USERNAME_PREDEFINITO = AppConstants.USERNAME_PREDEFINITO;
    public static final String PASSWORD_PREDEFINITA = "config";

    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 4;

    private final IUtenteRepository repo;
    private final Credenziali utenti;

    /**
     * @pre repo   != null
     * @pre utenti != null
     */
    public AuthenticationService(IUtenteRepository repo)
    {
        this.repo   = Objects.requireNonNull(repo);
        this.utenti = Objects.requireNonNull(utenti);
    }

    // ---------------------------------------------------------------
    // CONFIGURATORE
    // ---------------------------------------------------------------

    /**
     * Attempts login for a configuratore.
     *
     * @pre  username != null
     * @pre  password != null
     * @post returns Configuratore if credentials are valid, null otherwise
     */
    public Configuratore loginConfiguratore(String username, String password)
    {
        if (username == null || password == null)
            return null;

        if (username.equals(USERNAME_PREDEFINITO) && password.equals(PASSWORD_PREDEFINITA))
            return new Configuratore(USERNAME_PREDEFINITO);

        String passSalvata = utenti.getConfiguratori().get(username);
        if (passSalvata != null && passSalvata.equals(password))
            return new Configuratore(username);

        return null;
    }

    /**
     * Registers a new configuratore with personal credentials.
     * Username must be unique across both configuratori AND fruitori.
     *
     * @pre  newUsername != null &amp;&amp; newUsername.trim().length() &gt;= 3
     * @pre  newPassword != null &amp;&amp; newPassword.trim().length() &gt;= 4
     * @post utenti.getConfiguratori().containsKey(newUsername)
     * @throws IllegalArgumentException if credentials invalid or username taken
     */
    public Configuratore registraNuovoConfiguratore(String newUsername, String newPassword)
    {
        validaCredenziali(newUsername, newPassword);

        if (usernameGiaEsistente(newUsername))
            throw new IllegalArgumentException("Username già esistente.");

        if (USERNAME_PREDEFINITO.equalsIgnoreCase(newUsername))
            throw new IllegalArgumentException("Username non consentito (riservato).");

        utenti.addConfiguratore(newUsername, newPassword);
        repo.save(utenti);

        return new Configuratore(newUsername);
    }

    public boolean esistonoConfiguratori()
    {
        return !utenti.getConfiguratori().isEmpty();
    }

    // ---------------------------------------------------------------
    // FRUITORE
    // ---------------------------------------------------------------

    /**
     * Attempts login for a fruitore.
     *
     * @pre  username != null
     * @pre  password != null
     * @post returns Fruitore if credentials are valid, null otherwise
     */
    public Fruitore loginFruitore(String username, String password)
    {
        if (username == null || password == null)
            return null;

        String saved = utenti.getFruitori().get(username);

        if (saved != null && saved.equals(password))
            return new Fruitore(username);

        return null;
    }

    /**
     * Registers a new fruitore.
     * Username must be unique across both fruitori AND configuratori.
     *
     * @pre  username != null &amp;&amp; username.trim().length() >= 3
     * @pre  password != null &amp;&amp; password.trim().length() >= 4
     * @post utenti.getFruitori().containsKey(username)
     * @throws IllegalArgumentException if credentials invalid or username taken
     */
    public Fruitore registraFruitore(String username, String password)
    {
        validaCredenziali(username, password);

        if (usernameGiaEsistente(username))
            throw new IllegalArgumentException("Username già esistente.");

        utenti.addFruitore(username, password);
        repo.save(utenti);

        return new Fruitore(username);
    }

    // ---------------------------------------------------------------
    // UTILITY
    // ---------------------------------------------------------------

    private boolean usernameGiaEsistente(String username)
    {
        if (utenti.getFruitori().containsKey(username))
            return true;

        if (utenti.getConfiguratori().containsKey(username))
            return true;

        if (USERNAME_PREDEFINITO.equalsIgnoreCase(username))
            return true;

        return false;
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
