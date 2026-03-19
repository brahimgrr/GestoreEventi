package it.unibs.ingsoft.v4.persistence;

/**
 * Repository interface for per-user notifications.
 */
public interface INotificaRepository
{
    /**
     * Loads notification data from persistent storage.
     * Returns an empty {@link NotificaData} if no data exists yet.
     *
     * @return non-null NotificaData
     */
    NotificaData load();

    /**
     * Persists the given notification data snapshot.
     *
     * @pre notificaData != null
     */
    void save(NotificaData notificaData);
}
