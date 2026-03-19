package it.unibs.ingsoft.v5.service;

import it.unibs.ingsoft.v5.model.Notifica;
import it.unibs.ingsoft.v5.persistence.INotificaRepository;
import it.unibs.ingsoft.v5.persistence.NotificaData;

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

    public void aggiungiNotifica(String username, String messaggio)
    {
        Objects.requireNonNull(username,  "Username non può essere null.");
        Objects.requireNonNull(messaggio, "Messaggio non può essere null.");

        notificaData.addNotifica(username, new Notifica(messaggio, LocalDate.now()));
    }

    public void aggiungiNotificaESalva(String username, String messaggio)
    {
        aggiungiNotifica(username, messaggio);
        repo.save(notificaData);
    }

    public void salva()
    {
        repo.save(notificaData);
    }

    public List<Notifica> getNotifiche(String username)
    {
        Objects.requireNonNull(username, "Username non può essere null.");
        List<Notifica> lista = notificaData.getNotifiche().get(username);
        return lista == null ? Collections.emptyList() : Collections.unmodifiableList(lista);
    }

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
