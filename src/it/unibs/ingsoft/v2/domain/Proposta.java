package it.unibs.ingsoft.v2.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an event proposal.
 *
 * <p>Lifecycle: BOZZA → VALIDA → APERTA (terminal).
 * Call {@link it.unibs.ingsoft.v2.application.PropostaService#validaProposta}
 * to transition to VALIDA; call
 * {@link it.unibs.ingsoft.v2.application.PropostaService#pubblicaProposta}
 * to transition to APERTA and persist.</p>
 */
public final class Proposta
{
    private final Categoria             categoria;
    private final Map<String, String>   valoriCampi;
    private StatoProposta               stato;
    private LocalDate                   dataPubblicazione;
    private LocalDate                   termineIscrizione;
    private LocalDate                   dataEvento;

    /**
     * Creates a new draft proposal.
     *
     * @pre categoria != null
     */
    public Proposta(Categoria categoria)
    {
        if (categoria == null)
            throw new IllegalArgumentException("La categoria non può essere null.");
        this.categoria   = categoria;
        this.valoriCampi = new HashMap<>();
        this.stato       = StatoProposta.BOZZA;
    }

    /** Jackson deserialisation factory — reconstructs a fully populated (published) proposal. */
    @JsonCreator
    public static Proposta fromJson(
            @JsonProperty("categoria")         Categoria           categoria,
            @JsonProperty("valoriCampi")       Map<String, String> valoriCampi,
            @JsonProperty("stato")             StatoProposta       stato,
            @JsonProperty("dataPubblicazione") LocalDate           dataPubblicazione,
            @JsonProperty("termineIscrizione") LocalDate           termineIscrizione,
            @JsonProperty("dataEvento")        LocalDate           dataEvento)
    {
        Proposta p = new Proposta(categoria);
        if (valoriCampi != null)      p.valoriCampi.putAll(valoriCampi);
        if (stato != null)            p.stato             = stato;
        if (dataPubblicazione != null) p.dataPubblicazione = dataPubblicazione;
        if (termineIscrizione != null) p.termineIscrizione = termineIscrizione;
        if (dataEvento != null)        p.dataEvento        = dataEvento;
        return p;
    }

    public Categoria            getCategoria()         { return categoria; }
    public StatoProposta        getStato()             { return stato; }
    public LocalDate            getDataPubblicazione() { return dataPubblicazione; }
    public LocalDate            getTermineIscrizione() { return termineIscrizione; }
    public LocalDate            getDataEvento()        { return dataEvento; }

    public Map<String, String> getValoriCampi()
    {
        return Collections.unmodifiableMap(valoriCampi);
    }

    public void putAllValoriCampi(Map<String, String> valori)
    {
        valoriCampi.putAll(valori);
    }

    public void setDataPubblicazione(LocalDate d) { this.dataPubblicazione = d; }
    public void setTermineIscrizione(LocalDate d) { this.termineIscrizione = d; }
    public void setDataEvento(LocalDate d)        { this.dataEvento = d; }

    /**
     * Transitions to the given state.
     *
     * @throws IllegalStateException if the transition is not allowed
     */
    public void setStato(StatoProposta next)
    {
        if (next == null)
            throw new IllegalArgumentException("Stato non può essere null.");
        if (!stato.canTransitionTo(next))
            throw new IllegalStateException("Transizione non valida: " + stato + " → " + next + ".");
        this.stato = next;
    }
}
