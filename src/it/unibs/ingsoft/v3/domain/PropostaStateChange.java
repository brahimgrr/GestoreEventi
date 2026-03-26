package it.unibs.ingsoft.v3.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public final class PropostaStateChange {
    private final StatoProposta stato;
    private final LocalDate dataCambio;

    @JsonCreator
    public PropostaStateChange(@JsonProperty("stato") StatoProposta stato,
                               @JsonProperty("dataCambio") LocalDate dataCambio) {
        this.stato = stato;
        this.dataCambio = dataCambio;
    }

    public StatoProposta getStato() {
        return stato;
    }

    public LocalDate getDataCambio() {
        return dataCambio;
    }

    @Override
    public String toString() {
        return "Stato: " + stato + " (dal " + dataCambio + ")";
    }
}
