package it.unibs.ingsoft.v3.model;

import java.io.Serializable;
import java.time.LocalDate;

public final class Notifica implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String    messaggio;
    private final LocalDate data;

    public Notifica(String messaggio, LocalDate data)
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