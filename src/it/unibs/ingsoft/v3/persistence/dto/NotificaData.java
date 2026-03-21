package it.unibs.ingsoft.v3.persistence.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.unibs.ingsoft.v3.domain.Notifica;

import java.util.*;

/**
 * JSON-serializable DTO for per-user notification queues.
 */
public final class NotificaData
{
    private final Map<String, List<Notifica>> notifiche = new HashMap<>();

    public NotificaData() {}

    /** Jackson deserialisation factory. */
    @JsonCreator
    public static NotificaData fromJson(
            @JsonProperty("notifiche") Map<String, List<Notifica>> notifiche)
    {
        NotificaData d = new NotificaData();
        if (notifiche != null)
        {
            notifiche.forEach((username, lista) -> {
                if (lista != null) lista.forEach(n -> d.addNotifica(username, n));
            });
        }
        return d;
    }

    public Map<String, List<Notifica>> getNotifiche()
    {
        return Collections.unmodifiableMap(notifiche);
    }

    public void addNotifica(String username, Notifica n)
    {
        notifiche.computeIfAbsent(username, k -> new ArrayList<>()).add(n);
    }

    public void removeNotifica(String username, int index)
    {
        List<Notifica> lista = notifiche.get(username);
        if (lista == null || index < 0 || index >= lista.size())
            throw new IndexOutOfBoundsException("Indice notifica non valido: " + index);
        lista.remove(index);
    }
}
