package it.unibs.ingsoft.v3.service;

import it.unibs.ingsoft.v3.model.Configuratore;
import it.unibs.ingsoft.v3.persistence.FruitoreData;
import it.unibs.ingsoft.v3.persistence.IUtenteRepository;
import it.unibs.ingsoft.v3.persistence.UtenteData;

import java.util.Objects;

public final class AuthenticationService
{
    public static final String USERNAME_PREDEFINITO = "config";
    public static final String PASSWORD_PREDEFINITA = "config";

    private final IUtenteRepository repo;
    private final UtenteData        utenti;
    private final FruitoreData      fruitori;

    /**
     * @pre repo     != null
     * @pre utenti   != null
     * @pre fruitori != null
     */
    public AuthenticationService(IUtenteRepository repo, UtenteData utenti, FruitoreData fruitori)
    {
        this.repo     = Objects.requireNonNull(repo);
        this.utenti   = Objects.requireNonNull(utenti);
        this.fruitori = Objects.requireNonNull(fruitori);
    }

    public Configuratore login(String username, String password)
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
     * @pre  newUsername != null &amp;&amp; newUsername.trim().length() &gt;= 3
     * @pre  newPassword != null &amp;&amp; newPassword.trim().length() &gt;= 4
     * @throws IllegalArgumentException if credentials invalid or username taken
     */
    public Configuratore registraNuovoConfiguratore(String newUsername, String newPassword)
    {
        validaCredenziali(newUsername, newPassword);

        if (utenti.getConfiguratori().containsKey(newUsername))
            throw new IllegalArgumentException("Username già esistente (usato da un configuratore).");

        if (fruitori.getFruitori().containsKey(newUsername))
            throw new IllegalArgumentException("Username già esistente (usato da un fruitore).");

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
