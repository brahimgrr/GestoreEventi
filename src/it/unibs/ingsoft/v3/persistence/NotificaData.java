package it.unibs.ingsoft.v3.persistence;

import it.unibs.ingsoft.v3.model.Notifica;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * Pure serializable DTO for per-user notification queues.
 */
public final class NotificaData implements Serializable
{
    @Serial private static final long serialVersionUID = 1L;

    private final Map<String, List<Notifica>> notifiche = new HashMap<>();

    /**
     * Returns an unmodifiable view of the notification map.
     * The inner lists remain mutable to allow removal operations.
     */
    public Map<String, List<Notifica>> getNotifiche()
    {
        return Collections.unmodifiableMap(notifiche);
    }

    public void addNotifica(String username, Notifica n)
    {
        notifiche.computeIfAbsent(username, k -> new ArrayList<>()).add(n);
    }
}
