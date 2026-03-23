package it.unibs.ingsoft.v3.application;

import it.unibs.ingsoft.v3.domain.Notifica;
import it.unibs.ingsoft.v3.domain.SpazioPersonale;
import it.unibs.ingsoft.v3.persistence.api.ISpazioPersonaleRepository;

import java.util.List;
import java.util.Objects;

/**
 * Manages notifications for users.
 */
public final class NotificationService {

    private final ISpazioPersonaleRepository repo;

    public NotificationService(ISpazioPersonaleRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    /**
     * Invia una notifica a un determinato fruitore.
     * @param username username del fruitore
     * @param notifica la notifica da salvare
     */
    public void inviaNotifica(String username, Notifica notifica) {
        if (username == null || notifica == null) return;
        SpazioPersonale sp = repo.get(username);
        sp.addNotifica(notifica);
        repo.save();
    }

    /**
     * Ritorna tutte le notifiche di un fruitore.
     */
    public List<Notifica> getNotifiche(String username) {
        return repo.get(username).getNotifiche();
    }

    /**
     * Cancella una specifica notifica.
     */
    public void cancellaNotifica(String username, Notifica notifica) {
        SpazioPersonale sp = repo.get(username);
        sp.removeNotifica(notifica);
        repo.save();
    }
}
