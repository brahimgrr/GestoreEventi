package it.unibs.ingsoft.v2.model;

import java.io.Serializable;
import java.time.LocalDate;
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

    public Proposta(Categoria categoria)
    {
        this.categoria = categoria;
        this.valoriCampi = new HashMap<>();
        this.stato = StatoProposta.BOZZA;
    }

    public Categoria getCategoria()
    {
        return categoria;
    }

    public Map<String, String> getValoriCampi()
    {
        return valoriCampi;
    }

    public StatoProposta getStato()
    {
        return stato;
    }

    public void setStato(StatoProposta stato)
    {
        this.stato = stato;
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