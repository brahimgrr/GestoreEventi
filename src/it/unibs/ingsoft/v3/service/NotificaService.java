package it.unibs.ingsoft.v3.service;

import it.unibs.ingsoft.v3.model.Notifica;
import it.unibs.ingsoft.v3.persistence.INotificaRepository;
import it.unibs.ingsoft.v3.persistence.NotificaData;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Manages the personal notification space (spazio personale) of each fruitore.
 */
public final class NotificaService
{
    private final INotificaRepository repo;
    private final NotificaData        notificaData;

    /**
     * @pre repo         != null
     * @pre notificaData != null
     */
    public NotificaService(INotificaRepository repo, NotificaData notificaData)
    {
        this.repo         = Objects.requireNonNull(repo);
        this.notificaData = Objects.requireNonNull(notificaData);
    }

    /**
     * Adds a notification without saving.
     * The caller is responsible for calling {@link #salva()} after all
     * notifications in a batch have been added.
     *
     * @pre username  != null
     * @pre messaggio != null
     */
    public void aggiungiNotifica(String username, String messaggio)
    {
        Objects.requireNonNull(username,  "Username non può essere null.");
        Objects.requireNonNull(messaggio, "Messaggio non può essere null.");

        notificaData.addNotifica(username, new Notifica(messaggio, LocalDate.now()));
    }

    /**
     * Adds a notification and immediately persists the change.
     *
     * @pre username  != null
     * @pre messaggio != null
     */
    public void aggiungiNotificaESalva(String username, String messaggio)
    {
        aggiungiNotifica(username, messaggio);
        repo.save(notificaData);
    }

    /**
     * Persists all pending notification changes.
     * Call this after a batch of {@link #aggiungiNotifica} calls.
     */
    public void salva()
    {
        repo.save(notificaData);
    }

    /**
     * Returns an unmodifiable view of the notifications for the given user.
     *
     * @pre username != null
     * @return unmodifiable list; empty if none
     */
    public List<Notifica> getNotifiche(String username)
    {
        Objects.requireNonNull(username, "Username non può essere null.");
        List<Notifica> lista = notificaData.getNotifiche().get(username);
        return lista == null ? Collections.emptyList() : Collections.unmodifiableList(lista);
    }

    /**
     * Removes the notification at the given index and persists.
     *
     * @pre username != null
     * @pre index >= 0 &amp;&amp; index &lt; getNotifiche(username).size()
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public void eliminaNotifica(String username, int index)
    {
        Objects.requireNonNull(username, "Username non può essere null.");
        List<Notifica> lista = notificaData.getNotifiche().get(username);

        if (lista == null || index < 0 || index >= lista.size())
            throw new IndexOutOfBoundsException("Indice notifica non valido: " + index);

        lista.remove(index);
        repo.save(notificaData);
    }
}
