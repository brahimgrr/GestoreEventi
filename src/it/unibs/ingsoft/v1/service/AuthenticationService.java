package it.unibs.ingsoft.v1.service;

import it.unibs.ingsoft.v1.model.Configuratore;
import it.unibs.ingsoft.v1.persistence.AppData;
import it.unibs.ingsoft.v1.persistence.IPersistenceService;

import java.util.Objects;

public final class AuthenticationService
{
    public static final String USERNAME_PREDEFINITO = "config";
    public static final String PASSWORD_PREDEFINITA = "config";
    private final IPersistenceService db;
    private final AppData data;

    /**
     * @pre db   != null
     * @pre data != null
     */
    public AuthenticationService(IPersistenceService db, AppData data)
    {
        this.db = Objects.requireNonNull(db);
        this.data = Objects.requireNonNull(data);
    }

    /**
     * Attempts to log in with the given credentials.
     *
     * @pre  username != null
     * @pre  password != null
     * @post returns a {@link Configuratore} instance if credentials match, {@code null} otherwise
     * @param username the username
     * @param password the password
     * @return the authenticated {@link Configuratore}, or {@code null} if credentials are wrong
     */
    public Configuratore login(String username, String password)
    {
        if (username == null || password == null)
            return null;

        //CASO CREDENZIALI PREDEFINITE
        if (username.equals(USERNAME_PREDEFINITO) && password.equals(PASSWORD_PREDEFINITA))
            return new Configuratore(USERNAME_PREDEFINITO);

        // CASO: CREDENZIALI SALVATE
        String passSalvata = data.getConfiguratori().get(username);
        if (passSalvata != null && passSalvata.equals(password))
            return new Configuratore(username);

        return null;
    }

    /**
     * Registers a new configurator with the given personal credentials.
     * Must be called immediately after the first login with default credentials.
     *
     * @pre  newUsername != null &amp;&amp; newUsername.trim().length() &gt;= 3
     * @pre  newPassword != null &amp;&amp; newPassword.trim().length() &gt;= 4
     * @pre  !newUsername.equalsIgnoreCase({@value #USERNAME_PREDEFINITO})
     * @post data.getConfiguratori().containsKey(newUsername)
     * @param newUsername the desired username (min 3 chars)
     * @param newPassword the desired password (min 4 chars)
     * @return the newly created {@link Configuratore}
     * @throws IllegalArgumentException if credentials are too short, reserved, or already taken
     */
    public Configuratore registraNuovoConfiguratore(String newUsername, String newPassword)
    {
        //Username almeno 3 caratteri e password almeno 3 caratteri
        validaCredenziali(newUsername, newPassword);

        if (data.getConfiguratori().containsKey(newUsername))
            throw new IllegalArgumentException("Username già esistente.");

        //NON ACCETTO CONFIG COME USERNAME
        if (USERNAME_PREDEFINITO.equalsIgnoreCase(newUsername))
            throw new IllegalArgumentException("Username non consentito (riservato).");

        data.addConfiguratore(newUsername, newPassword);
        db.save(data);

        return new Configuratore(newUsername);
    }

    public boolean esistonoConfiguratori()
    {
        return !data.getConfiguratori().isEmpty();
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