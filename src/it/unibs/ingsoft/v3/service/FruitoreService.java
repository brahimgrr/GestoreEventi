package it.unibs.ingsoft.v3.service;

import it.unibs.ingsoft.v3.model.Fruitore;
import it.unibs.ingsoft.v3.persistence.AppData;
import it.unibs.ingsoft.v3.persistence.IPersistenceService;

import java.util.Objects;

public final class FruitoreService implements NotificaListener
{
    private final IPersistenceService db;
    private final AppData             data;
    private final NotificaService     notificaService;

    /**
     * @pre db              != null
     * @pre data            != null
     * @pre notificaService != null
     */
    public FruitoreService(IPersistenceService db, AppData data, NotificaService notificaService)
    {
        this.db              = Objects.requireNonNull(db);
        this.data            = Objects.requireNonNull(data);
        this.notificaService = Objects.requireNonNull(notificaService);
    }

    // ---------------------------------------------------------------
    // AUTHENTICATION
    // ---------------------------------------------------------------

    /**
     * Registers a new fruitore.
     * Username must be unique across both fruitori AND configuratori.
     *
     * @pre  username != null &amp;&amp; username.trim().length() >= 3
     * @pre  password != null &amp;&amp; password.trim().length() >= 4
     * @post data.getFruitori().containsKey(username)
     * @throws IllegalArgumentException if credentials invalid or username taken
     */
    public Fruitore registraFruitore(String username, String password)
    {
        validaCredenziali(username, password);

        if (usernameGiaEsistente(username))
            throw new IllegalArgumentException("Username già esistente.");

        data.addFruitore(username, password);
        db.save(data);

        return new Fruitore(username);
    }

    /**
     * Attempts login for a fruitore.
     * Returns the Fruitore if credentials are valid, null otherwise.
     *
     * @pre  username != null
     * @pre  password != null
     * @post returns Fruitore if credentials are valid, null otherwise
     */
    public Fruitore login(String username, String password)
    {
        if (username == null || password == null)
            return null;

        String saved = data.getFruitori().get(username);

        if (saved != null && saved.equals(password))
            return new Fruitore(username);

        return null;
    }

    // ---------------------------------------------------------------
    // NotificaListener implementation
    // ---------------------------------------------------------------

    /**
     * Observer implementation: adds a notification without saving.
     * Persistence is delegated to the caller (IscrizioneService)
     * so that bulk operations cause only one db.save.
     */
    @Override
    public void notifica(String usernameFruitore, String messaggio)
    {
        notificaService.aggiungiNotifica(usernameFruitore, messaggio);
    }

    // ---------------------------------------------------------------
    // UTILITY
    // ---------------------------------------------------------------

    /**
     * Checks if a username already exists among fruitori OR configuratori.
     * This guarantees global uniqueness as required by the assignment.
     */
    private boolean usernameGiaEsistente(String username)
    {
        if (data.getFruitori().containsKey(username))
            return true;

        if (data.getConfiguratori().containsKey(username))
            return true;

        // Also block the default configuratore username
        if (AuthenticationService.USERNAME_PREDEFINITO.equalsIgnoreCase(username))
            return true;

        return false;
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
