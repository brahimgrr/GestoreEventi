package it.unibs.ingsoft.v4.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public final class Notifica {
    private final String id;
    private final String messaggio;
    private final LocalDateTime dataCreazione;

    @JsonCreator
    public Notifica(@JsonProperty("id") String id,
                    @JsonProperty("messaggio") String messaggio, 
                    @JsonProperty("dataCreazione") LocalDateTime dataCreazione) {
        this.id = id;
        this.messaggio = messaggio;
        this.dataCreazione = dataCreazione;
    }

    public Notifica(String messaggio) {
        this(java.util.UUID.randomUUID().toString(), messaggio, LocalDateTime.now());
    }

    public String getId() {
        return id;
    }

    public String getMessaggio() {
        return messaggio;
    }

    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notifica notifica = (Notifica) o;
        return id.equals(notifica.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
