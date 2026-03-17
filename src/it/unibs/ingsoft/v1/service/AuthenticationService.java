package it.unibs.ingsoft.v1.service;

import it.unibs.ingsoft.v1.model.Configuratore;
import it.unibs.ingsoft.v1.persistence.AppData;
import it.unibs.ingsoft.v1.persistence.DatabaseService;

import java.util.Objects;

public final class AuthenticationService
{
    public static final String USERNAME_PREDEFINITO = "config";
    public static final String PASSWORD_PREDEFINITA = "config";

    private final DatabaseService db;
    private final AppData         data;

    public AuthenticationService(DatabaseService db, AppData data)
    {
        this.db   = Objects.requireNonNull(db);
        this.data = Objects.requireNonNull(data);
    }

    // ---------------------------------------------------------------
    // LOGIN
    // ---------------------------------------------------------------

    public Configuratore login(String username, String password)
    {
        if (username == null || password == null)
            return null;

        // Credenziali predefinite
        if (username.equals(USERNAME_PREDEFINITO) && password.equals(PASSWORD_PREDEFINITA))
            return new Configuratore(USERNAME_PREDEFINITO);

        // Credenziali salvate
        String passSalvata = data.getConfiguratori().get(username);

        if (passSalvata != null && passSalvata.equals(password))
            return new Configuratore(username);

        return null;
    }

    // ---------------------------------------------------------------
    // REGISTRAZIONE
    // ---------------------------------------------------------------

    /**
     * Registra un nuovo configuratore con credenziali personalizzate.
     * Da chiamare subito dopo il primo login con credenziali predefinite.
     */
    public Configuratore registraNuovoConfiguratore(String newUsername, String newPassword)
    {
        validaCredenziali(newUsername, newPassword);

        if (data.getConfiguratori().containsKey(newUsername))
            throw new IllegalArgumentException("Username già esistente.");

        if (USERNAME_PREDEFINITO.equalsIgnoreCase(newUsername))
            throw new IllegalArgumentException("Username non consentito (riservato).");

        data.getConfiguratori().put(newUsername, newPassword);
        db.save(data);

        return new Configuratore(newUsername);
    }

    public boolean esistonoConfiguratori()
    {
        return !data.getConfiguratori().isEmpty();
    }

    // ---------------------------------------------------------------
    // UTILITY
    // ---------------------------------------------------------------

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