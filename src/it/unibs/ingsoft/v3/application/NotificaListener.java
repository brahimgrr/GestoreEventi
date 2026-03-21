package it.unibs.ingsoft.v3.application;

/**
 * Observer interface for proposal state-change notifications.
 * Decouples IscrizioneService (proposal management) from
 * NotificaService (notification management) as recommended by the spec.
 *
 * Implementations must add the notification to the recipient's inbox
 * and persist it when {@link #commit()} is called.
 * This follows the Observer pattern: IscrizioneService is the publisher,
 * each NotificaListener implementation is an independent channel.
 */
public interface NotificaListener
{
    /**
     * Deliver a notification to the named fruitore.
     * The notification may be buffered in memory until {@link #commit()} is called.
     *
     * @pre username != null
     * @pre messaggio != null
     */
    void notifica(String username, String messaggio);

    /**
     * Persist any notifications buffered since the last call to this method.
     * Called once by the publisher after a batch of {@link #notifica} calls.
     */
    void commit();
}
