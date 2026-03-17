package it.unibs.ingsoft.v5.service;

/**
 * Observer interface for proposal state-change notifications.
 * Decouples IscrizioneService (proposal management) from
 * FruitoreService (user management) as recommended by the spec.
 *
 * Implementations must add the notification to the recipient's inbox.
 * Persistence (db.save) is the responsibility of the caller.
 */
public interface NotificaListener
{
    /**
     * Deliver a notification to the named fruitore.
     *
     * @pre username != null
     * @pre messaggio != null
     */
    void notifica(String username, String messaggio);
}
