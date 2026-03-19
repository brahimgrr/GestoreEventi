package it.unibs.ingsoft.v3.service;

import it.unibs.ingsoft.v3.model.Fruitore;
import it.unibs.ingsoft.v3.persistence.FruitoreData;
import it.unibs.ingsoft.v3.persistence.IFruitoreRepository;
import it.unibs.ingsoft.v3.persistence.UtenteData;

import java.util.Objects;

public final class FruitoreService implements NotificaListener
{
    private final IFruitoreRepository fruitoreRepo;
    private final FruitoreData        fData;
    private final UtenteData          utenti;
    private final NotificaService     notificaService;

    /**
     * @pre fruitoreRepo    != null
     * @pre fData           != null
     * @pre utenti          != null  (read-only, for cross-namespace uniqueness check)
     * @pre notificaService != null
     */
    public FruitoreService(IFruitoreRepository fruitoreRepo, FruitoreData fData,
                           UtenteData utenti, NotificaService notificaService)
    {
        this.fruitoreRepo   = Objects.requireNonNull(fruitoreRepo);
        this.fData          = Objects.requireNonNull(fData);
        this.utenti         = Objects.requireNonNull(utenti);
        this.notificaService = Objects.requireNonNull(notificaService);
    }

    /**
     * Registers a new fruitore.
     * Username must be unique across both fruitori AND configuratori.
     *
     * @pre  username != null &amp;&amp; username.trim().length() >= 3
     * @pre  password != null &amp;&amp; password.trim().length() >= 4
     * @post fData.getFruitori().containsKey(username)
     * @throws IllegalArgumentException if credentials invalid or username taken
     */
    public Fruitore registraFruitore(String username, String password)
    {
        validaCredenziali(username, password);

        if (usernameGiaEsistente(username))
            throw new IllegalArgumentException("Username già esistente.");

        fData.addFruitore(username, password);
        fruitoreRepo.save(fData);

        return new Fruitore(username);
    }

    /**
     * Attempts login for a fruitore.
     *
     * @pre  username != null
     * @pre  password != null
     * @post returns Fruitore if credentials are valid, null otherwise
     */
    public Fruitore login(String username, String password)
    {
        if (username == null || password == null)
            return null;

        String saved = fData.getFruitori().get(username);

        if (saved != null && saved.equals(password))
            return new Fruitore(username);

        return null;
    }

    // ---------------------------------------------------------------
    // NotificaListener implementation
    // ---------------------------------------------------------------

    /**
     * Adds a notification without saving; persistence is delegated to the caller.
     */
    @Override
    public void notifica(String usernameFruitore, String messaggio)
    {
        notificaService.aggiungiNotifica(usernameFruitore, messaggio);
    }

    // ---------------------------------------------------------------
    // UTILITY
    // ---------------------------------------------------------------

    private boolean usernameGiaEsistente(String username)
    {
        if (fData.getFruitori().containsKey(username))
            return true;

        if (utenti.getConfiguratori().containsKey(username))
            return true;

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
