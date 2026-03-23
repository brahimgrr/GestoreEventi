package it.unibs.ingsoft.v3.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public final class ArchivioNotifiche {
    private final Map<String, SpazioPersonale> utenti;

    public ArchivioNotifiche() {
        this.utenti = new HashMap<>();
    }

    @JsonCreator
    public static ArchivioNotifiche fromJson(
            @JsonProperty("utenti") Map<String, SpazioPersonale> utenti) {
        ArchivioNotifiche a = new ArchivioNotifiche();
        if (utenti != null) {
            a.utenti.putAll(utenti);
        }
        return a;
    }

    public Map<String, SpazioPersonale> getUtenti() {
        return utenti;
    }

    public SpazioPersonale getSpazioDi(String username) {
        return utenti.computeIfAbsent(username, k -> new SpazioPersonale());
    }
}
