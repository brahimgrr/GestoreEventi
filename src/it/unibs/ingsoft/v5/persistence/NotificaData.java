package it.unibs.ingsoft.v5.persistence;

import it.unibs.ingsoft.v5.model.Notifica;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure serializable DTO holding per-user notification lists.
 */
public final class NotificaData implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final Map<String, List<Notifica>> notifiche = new HashMap<>();

    public Map<String, List<Notifica>> getNotifiche()
    {
        return Collections.unmodifiableMap(notifiche);
    }

    public void addNotifica(String username, Notifica n)
    {
        notifiche.computeIfAbsent(username, k -> new ArrayList<>()).add(n);
    }
}
