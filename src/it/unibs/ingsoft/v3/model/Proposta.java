package it.unibs.ingsoft.v3.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

public class Proposta implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Categoria                    categoria;
    private Map<String, String>          valoriCampi;
    private StatoProposta                stato;
    private LocalDate                    dataPubblicazione;
    private LocalDate                    termineIscrizione;
    private LocalDate                    dataEvento;
    private LocalDate                    dataConclus;

    // NEW IN V3
    private final List<Iscrizione>              iscrizioni     = new ArrayList<>();
    private final Map<StatoProposta, LocalDate> storiaStati    = new LinkedHashMap<>();

    public Proposta(Categoria categoria)
    {
        this.categoria   = categoria;
        this.valoriCampi = new HashMap<>();
        this.stato       = StatoProposta.BOZZA;
    }

    // ---- existing getters/setters ----

    public Categoria getCategoria()                   { return categoria; }
    public Map<String, String> getValoriCampi()       { return valoriCampi; }
    public StatoProposta getStato()                   { return stato; }
    public LocalDate getDataPubblicazione()           { return dataPubblicazione; }
    public LocalDate getTermineIscrizione()           { return termineIscrizione; }
    public LocalDate getDataEvento()                  { return dataEvento; }
    public LocalDate getDataConclus()                 { return dataConclus; }

    public void setDataPubblicazione(LocalDate d)     { this.dataPubblicazione = d; }
    public void setTermineIscrizione(LocalDate d)     { this.termineIscrizione = d; }
    public void setDataEvento(LocalDate d)            { this.dataEvento = d; }
    public void setDataConclus(LocalDate d)           { this.dataConclus = d; }

    /**
     * Changes state and records the transition date in the history.
     */
    public void setStato(StatoProposta stato, LocalDate data)
    {
        this.stato = stato;
        storiaStati.put(stato, data);
    }

    // ---- NEW V3 methods ----

    public List<Iscrizione> getIscrizioni()
    {
        return Collections.unmodifiableList(iscrizioni);
    }

    public void addIscrizione(Iscrizione i)
    {
        iscrizioni.add(i);
    }

    public boolean removeIscrizione(String usernameFruitore)
    {
        return iscrizioni.removeIf(
                i -> i.getFruitore().getUsername().equalsIgnoreCase(usernameFruitore)
        );
    }

    public boolean isIscrittoFruitore(String usernameFruitore)
    {
        return iscrizioni.stream()
                .anyMatch(i -> i.getFruitore().getUsername().equalsIgnoreCase(usernameFruitore));
    }

    public int getNumeroIscritti()
    {
        return iscrizioni.size();
    }

    public Map<StatoProposta, LocalDate> getStoriaStati()
    {
        return Collections.unmodifiableMap(storiaStati);
    }
}