package it.unibs.ingsoft.v5.service;

import it.unibs.ingsoft.v5.model.Fruitore;
import it.unibs.ingsoft.v5.persistence.FruitoreData;
import it.unibs.ingsoft.v5.persistence.IFruitoreRepository;
import it.unibs.ingsoft.v5.persistence.UtenteData;

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

    public Fruitore registraFruitore(String username, String password)
    {
        validaCredenziali(username, password);

        if (usernameGiaEsistente(username))
            throw new IllegalArgumentException("Username già esistente.");

        fData.addFruitore(username, password);
        repo.save(fData);

        return new Fruitore(username);
    }

    public Fruitore login(String username, String password)
    {
        if (username == null || password == null)
            return null;

        String saved = fData.getFruitori().get(username);

        if (saved != null && saved.equals(password))
            return new Fruitore(username);

        return null;
    }

    @Override
    public void notifica(String usernameFruitore, String messaggio)
    {
        notificaService.aggiungiNotifica(usernameFruitore, messaggio);
    }

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
