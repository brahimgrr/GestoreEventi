package it.unibs.ingsoft.v4.service;

import it.unibs.ingsoft.v4.model.Notifica;
import it.unibs.ingsoft.v4.persistence.AppData;
import it.unibs.ingsoft.v4.persistence.IPersistenceService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Manages the personal notification space (spazio personale) of each fruitore.
 * Extracted from FruitoreService to satisfy the Single Responsibility Principle (SRP).
 */
public final class NotificaService
{
    private final IPersistenceService db;
    private final AppData data;

    /**
     * @pre db   != null
     * @pre data != null
     */
    public NotificaService(IPersistenceService db, AppData data)
    {
        this.db   = Objects.requireNonNull(db);
        this.data = Objects.requireNonNull(data);
    }

    /**
     * Adds a notification to the recipient's personal space without saving.
     * @pre username != null
     * @pre messaggio != null
     * @post getNotifiche(username).size() is increased by 1
     */
    public void aggiungiNotifica(String username, String messaggio)
    {
        Objects.requireNonNull(username,  "Username non può essere null.");
        Objects.requireNonNull(messaggio, "Messaggio non può essere null.");
        data.addNotifica(username, new Notifica(messaggio, LocalDate.now()));
    }

    /**
     * Adds a notification and immediately persists the change.
     * @pre username != null
     * @pre messaggio != null
     */
    public void aggiungiNotificaESalva(String username, String messaggio)
    {
        aggiungiNotifica(username, messaggio);
        db.save(data);
    }

    /**
     * Returns an unmodifiable view of the notifications for the given user.
     * @pre username != null
     * @return unmodifiable list; empty if none
     */
    public List<Notifica> getNotifiche(String username)
    {
        Objects.requireNonNull(username, "Username non può essere null.");
        List<Notifica> lista = data.getNotifiche().get(username);
        return lista == null ? Collections.emptyList() : Collections.unmodifiableList(lista);
    }

    /**
     * Removes the notification at the given index from the user's space and persists.
     * @pre username != null
     * @pre index >= 0 && index < getNotifiche(username).size()
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public void eliminaNotifica(String username, int index)
    {
        Objects.requireNonNull(username, "Username non può essere null.");
        List<Notifica> lista = data.getNotifiche().get(username);
        if (lista == null || index < 0 || index >= lista.size())
            throw new IndexOutOfBoundsException("Indice notifica non valido: " + index);
        lista.remove(index);
        db.save(data);
    }
}
