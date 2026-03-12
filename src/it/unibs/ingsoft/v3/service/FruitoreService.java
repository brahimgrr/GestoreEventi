package it.unibs.ingsoft.v3.service;

import it.unibs.ingsoft.v3.model.Fruitore;
import it.unibs.ingsoft.v3.model.Notifica;
import it.unibs.ingsoft.v3.persistence.AppData;
import it.unibs.ingsoft.v3.persistence.DatabaseService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class FruitoreService
{
    private final DatabaseService db;
    private final AppData         data;

    public FruitoreService(DatabaseService db, AppData data)
    {
        this.db   = Objects.requireNonNull(db);
        this.data = Objects.requireNonNull(data);
    }

    // ---------------------------------------------------------------
    // AUTHENTICATION
    // ---------------------------------------------------------------

    /**
     * Registers a new fruitore.
     * Username must be unique across both fruitori AND configuratori.
     */
    public Fruitore registraFruitore(String username, String password)
    {
        validaCredenziali(username, password);

        if (usernameGiaEsistente(username))
            throw new IllegalArgumentException("Username già esistente.");

        data.getFruitori().put(username, password);
        db.save(data);

        return new Fruitore(username);
    }

    /**
     * Attempts login for a fruitore.
     * Returns the Fruitore if credentials are valid, null otherwise.
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
    // NOTIFICHE
    // ---------------------------------------------------------------

    /**
     * Sends a notification to a specific fruitore.
     */
    public void inviaNotifica(String usernameFruitore, String messaggio)
    {
        Notifica n = new Notifica(messaggio, LocalDate.now());
        data.getNotifichePerFruitore(usernameFruitore).add(n);
        db.save(data);
    }

    /**
     * Returns all notifications for a fruitore (unmodifiable).
     */
    public List<Notifica> getNotifiche(String usernameFruitore)
    {
        return Collections.unmodifiableList(
                data.getNotifichePerFruitore(usernameFruitore)
        );
    }

    /**
     * Deletes a notification by its index in the fruitore's list.
     * Returns true if deleted, false if index was invalid.
     */
    public boolean eliminaNotifica(String usernameFruitore, int index)
    {
        List<Notifica> lista = data.getNotifichePerFruitore(usernameFruitore);

        if (index < 0 || index >= lista.size())
            return false;

        lista.remove(index);
        db.save(data);
        return true;
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