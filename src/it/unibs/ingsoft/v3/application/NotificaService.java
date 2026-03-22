package it.unibs.ingsoft.v3.application;

import it.unibs.ingsoft.v3.domain.Notifica;
import it.unibs.ingsoft.v3.persistence.api.INotificaRepository;
import it.unibs.ingsoft.v3.domain.ArchivioNotifiche;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Manages the personal notification space (spazio personale) of each fruitore.
 */
public final class NotificaService implements NotificaListener
{
    private final INotificaRepository repo;
    private final ArchivioNotifiche archivioNotifiche;

    /**
     * @pre repo         != null
     * @pre notificaData != null
     */
    public NotificaService(INotificaRepository repo, ArchivioNotifiche archivioNotifiche)
    {
        this.repo         = Objects.requireNonNull(repo);
        this.archivioNotifiche = Objects.requireNonNull(archivioNotifiche);
    }

    // ---------------------------------------------------------------
    // NotificaListener implementation
    // ---------------------------------------------------------------

    /**
     * Delivers a notification without saving.
     * Call {@link #commit()} after a batch of notifications to persist them all at once.
     */
    @Override
    public void notifica(String username, String messaggio)
    {
        aggiungiNotifica(username, messaggio);
    }

    /**
     * Persists all notifications buffered since the last commit.
     * Called by IscrizioneService once per state-transition batch.
     */
    @Override
    public void commit()
    {
        salva();
    }

    // ---------------------------------------------------------------
    // NOTIFICATIONS
    // ---------------------------------------------------------------

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

        archivioNotifiche.addNotifica(username, new Notifica(messaggio, LocalDate.now()));
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
        repo.save(archivioNotifiche);
    }

    /**
     * Persists all pending notification changes.
     * Call this after a batch of {@link #aggiungiNotifica} calls.
     */
    public void salva()
    {
        repo.save(archivioNotifiche);
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
        List<Notifica> lista = archivioNotifiche.getNotifiche().get(username);
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
        archivioNotifiche.removeNotifica(username, index);
        repo.save(archivioNotifiche);
    }
}
