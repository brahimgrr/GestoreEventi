package it.unibs.ingsoft.v3.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * JSON-serializable DTO for per-user notification queues.
 */
public final class ArchivioNotifiche
{
    private final Map<String, List<Notifica>> notifiche = new HashMap<>();

    public ArchivioNotifiche() {}

    /** Jackson deserialisation factory. */
    @JsonCreator
    public static ArchivioNotifiche fromJson(
            @JsonProperty("notifiche") Map<String, List<Notifica>> notifiche)
    {
        ArchivioNotifiche d = new ArchivioNotifiche();
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
