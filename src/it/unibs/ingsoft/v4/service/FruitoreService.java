package it.unibs.ingsoft.v4.service;

import it.unibs.ingsoft.v4.model.Fruitore;
import it.unibs.ingsoft.v4.persistence.FruitoreData;
import it.unibs.ingsoft.v4.persistence.IFruitoreRepository;
import it.unibs.ingsoft.v4.persistence.UtenteData;

import java.util.Objects;

public final class FruitoreService implements NotificaListener
{
    private final IFruitoreRepository repo;
    private final FruitoreData        fData;
    private final UtenteData          utenti;
    private final NotificaService     notificaService;

    /**
     * @pre repo            != null
     * @pre fData           != null
     * @pre utenti          != null
     * @pre notificaService != null
     */
    public FruitoreService(IFruitoreRepository repo, FruitoreData fData,
                           UtenteData utenti, NotificaService notificaService)
    {
        this.repo            = Objects.requireNonNull(repo);
        this.fData           = Objects.requireNonNull(fData);
        this.utenti          = Objects.requireNonNull(utenti);
        this.notificaService = Objects.requireNonNull(notificaService);
    }

    // ---------------------------------------------------------------
    // AUTHENTICATION
    // ---------------------------------------------------------------

    /**
     * Registers a new fruitore.
     * Username must be unique across both fruitori AND configuratori.
     *
     * @pre username != null &amp;&amp; username.trim().length() &gt;= 3
     * @pre password != null &amp;&amp; password.trim().length() &gt;= 4
     * @throws IllegalArgumentException if credentials are invalid or username is already taken
     */
    public Fruitore registraFruitore(String username, String password)
    {
        validaCredenziali(username, password);

        if (usernameGiaEsistente(username))
            throw new IllegalArgumentException("Username già esistente.");

        fData.addFruitore(username, password);
        repo.save(fData);

        return new Fruitore(username);
    }

    /**
     * Attempts login for a fruitore.
     *
     * @pre username != null
     * @pre password != null
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
    // NOTIFICHE
    // ---------------------------------------------------------------

    /**
     * Observer implementation: delegates to NotificaService without saving.
     * Persistence is handled by the caller (IscrizioneService) for batching.
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
