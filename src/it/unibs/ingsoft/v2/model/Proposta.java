package it.unibs.ingsoft.v2.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Proposta implements Serializable
{

    private static final long serialVersionUID = 1L;

    private Categoria categoria;
    private Map<String,String> valoriCampi;
    private StatoProposta stato;
    private LocalDate dataPubblicazione;
    private LocalDate termineIscrizione;
    private LocalDate dataEvento;

    /**
     * Creates a new proposal in BOZZA (draft) state for the given category.
     *
     * @pre  categoria != null
     * @post getCategoria() == categoria
     * @post getStato() == StatoProposta.BOZZA
     * @post getValoriCampi().isEmpty()
     * @throws IllegalArgumentException if categoria is null
     */
    public Proposta(Categoria categoria)
    {
        if (categoria == null)
            throw new IllegalArgumentException("La categoria non può essere null.");

        this.categoria   = categoria;
        this.valoriCampi = new HashMap<>();
        this.stato       = StatoProposta.BOZZA;

        checkInvariant();
    }

    // -----------------------------------------------------------------------
    // Class invariant
    // -----------------------------------------------------------------------

    /**
     * Asserts the class invariant: must hold at the end of every constructor
     * and after every state-mutating operation.
     */
    private void checkInvariant()
    {
        if (categoria == null)
            throw new IllegalStateException("Invariant violated: categoria must not be null.");
        if (valoriCampi == null)
            throw new IllegalStateException("Invariant violated: valoriCampi map must not be null.");
        if (stato == null)
            throw new IllegalStateException("Invariant violated: stato must not be null.");
    }

    public Categoria getCategoria()
    {
        return categoria;
    }

    public Map<String, String> getValoriCampi()
    {
        return Collections.unmodifiableMap(valoriCampi);
    }

    public void putAllValoriCampi(Map<String, String> valori)
    {
        valoriCampi.putAll(valori);
    }

    public StatoProposta getStato()
    {
        return stato;
    }

    /**
     * @pre  stato != null
     * @post getStato() == stato
     * @throws IllegalArgumentException if stato is null
     */
    public void setStato(StatoProposta stato)
    {
        if (stato == null)
            throw new IllegalArgumentException("Stato non può essere null.");

        if (!this.stato.canTransitionTo(stato))
            throw new IllegalStateException("Transizione non valida: " + this.stato + " → " + stato + ".");

        this.stato = stato;
        checkInvariant();
    }

    public LocalDate getDataPubblicazione()
    {
        return dataPubblicazione;
    }

    public void setDataPubblicazione(LocalDate dataPubblicazione)
    {
        this.dataPubblicazione = dataPubblicazione;
    }

    public LocalDate getTermineIscrizione()
    {
        return termineIscrizione;
    }

    public void setTermineIscrizione(LocalDate termineIscrizione)
    {
        this.termineIscrizione = termineIscrizione;
    }

    public LocalDate getDataEvento()
    {
        return dataEvento;
    }

    public void setDataEvento(LocalDate dataEvento)
    {
        this.dataEvento = dataEvento;
    }

}