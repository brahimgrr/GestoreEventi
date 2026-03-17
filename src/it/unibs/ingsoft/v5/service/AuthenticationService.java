package it.unibs.ingsoft.v5.service;

import it.unibs.ingsoft.v5.model.Configuratore;
import it.unibs.ingsoft.v5.persistence.AppData;
import it.unibs.ingsoft.v5.persistence.IPersistenceService;

import java.util.Objects;

public final class AuthenticationService
{
    public static final String USERNAME_PREDEFINITO = "config";
    public static final String PASSWORD_PREDEFINITA = "config";

    private final IPersistenceService db;
    private final AppData data;

    /**
     * @pre db != null
     * @pre data != null
     */
    public AuthenticationService(IPersistenceService db, AppData data)
    {
        this.db = Objects.requireNonNull(db);
        this.data = Objects.requireNonNull(data);
    }

    /**
     * Attempts login for a configuratore.
     *
     * @pre  username != null
     * @pre  password != null
     * @post returns a non-null Configuratore if credentials are valid
     * @post returns null if credentials are invalid or either argument is null
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

    //REGISTRAZIONE NUOVO CONFIGURATORE
    //Da chiamare subito dopo il login con credenziali predefinite.
    /**
     * Registers a new configuratore with the given credentials.
     *
     * @pre  newUsername != null && !newUsername.isBlank() && newUsername.trim().length() >= 3
     * @pre  newPassword != null && !newPassword.isBlank() && newPassword.trim().length() >= 4
     * @pre  !esistonoConfiguratori() implies credentials are the default ones
     * @post data.getConfiguratori().containsKey(newUsername.trim())
     * @throws IllegalArgumentException if credentials are invalid or username is already taken
     */
    public Configuratore registraNuovoConfiguratore(String newUsername, String newPassword)
    {
        //Username almeno 3 caratteri e password almeno 3 caratteri
        validaCredenziali(newUsername, newPassword);

        if (data.getConfiguratori().containsKey(newUsername))
            throw new IllegalArgumentException("Username già esistente (usato da un configuratore).");

        if (data.getFruitori().containsKey(newUsername))
            throw new IllegalArgumentException("Username già esistente (usato da un fruitore).");

        //NON ACCETTO CONFIG COME USERNAME
        if (USERNAME_PREDEFINITO.equalsIgnoreCase(newUsername))
            throw new IllegalArgumentException("Username non consentito (riservato).");

        data.addConfiguratore(newUsername, newPassword);
        db.save(data);

        return new Configuratore(newUsername);
    }

    /**
     * Returns whether at least one non-default configuratore exists.
     *
     * @post result == !data.getConfiguratori().isEmpty()
     */
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