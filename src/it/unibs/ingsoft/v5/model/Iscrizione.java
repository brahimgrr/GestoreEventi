package it.unibs.ingsoft.v5.model;

import java.io.Serializable;
import java.time.LocalDate;

public final class Iscrizione implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final Fruitore fruitore;
    private final LocalDate dataIscrizione;

    public Iscrizione(Fruitore fruitore, LocalDate dataIscrizione)
    {
        this.fruitore       = fruitore;
        this.dataIscrizione = dataIscrizione;
    }

    public Fruitore getFruitore()       { return fruitore; }
    public LocalDate getDataIscrizione() { return dataIscrizione; }
}