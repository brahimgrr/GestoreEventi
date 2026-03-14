package it.unibs.ingsoft.v4.model;

import java.io.Serializable;
import java.time.LocalDate;

public final class Iscrizione implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final Fruitore fruitore;
    private final Proposta proposta;
    private final LocalDate dataIscrizione;

    public Iscrizione(Fruitore fruitore, Proposta proposta, LocalDate dataIscrizione)
    {
        this.fruitore       = fruitore;
        this.proposta       = proposta;
        this.dataIscrizione = dataIscrizione;
    }

    public Fruitore getFruitore()       { return fruitore; }
    public Proposta getProposta()       { return proposta; }
    public LocalDate getDataIscrizione() { return dataIscrizione; }
}