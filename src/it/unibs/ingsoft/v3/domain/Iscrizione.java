package it.unibs.ingsoft.v3.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public final class Iscrizione
{
    private final Fruitore  fruitore;
    private final LocalDate dataIscrizione;

    @JsonCreator
    public Iscrizione(@JsonProperty("fruitore")       Fruitore  fruitore,
                      @JsonProperty("dataIscrizione") LocalDate dataIscrizione)
    {
        this.fruitore       = fruitore;
        this.dataIscrizione = dataIscrizione;
    }

    public Fruitore  getFruitore()       { return fruitore; }
    public LocalDate getDataIscrizione() { return dataIscrizione; }
}
