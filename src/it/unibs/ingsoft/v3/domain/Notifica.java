package it.unibs.ingsoft.v3.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public final class Notifica
{
    private final String    messaggio;
    private final LocalDate data;

    @JsonCreator
    public Notifica(@JsonProperty("messaggio") String    messaggio,
                    @JsonProperty("data")      LocalDate data)
    {
        this.messaggio = messaggio;
        this.data      = data;
    }

    public String    getMessaggio() { return messaggio; }
    public LocalDate getData()      { return data; }

    @Override
    public String toString()
    {
        return "[" + data + "] " + messaggio;
    }
}
