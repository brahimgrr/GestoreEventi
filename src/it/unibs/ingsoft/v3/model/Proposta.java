package it.unibs.ingsoft.v3.model;

import java.io.IOException;
import java.io.ObjectInputStream;
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
    private List<Iscrizione>              iscrizioni  = new ArrayList<>();
    private Map<StatoProposta, LocalDate> storiaStati = new LinkedHashMap<>();

    /**
     * Creates a new proposal in BOZZA (draft) state for the given category.
     *
     * @pre  categoria != null
     * @post getCategoria() == categoria
     * @post getStato() == StatoProposta.BOZZA
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

    private void checkInvariant()
    {
        if (categoria == null)
            throw new IllegalStateException("Invariant violated: categoria must not be null.");
        if (valoriCampi == null)
            throw new IllegalStateException("Invariant violated: valoriCampi map must not be null.");
        if (stato == null)
            throw new IllegalStateException("Invariant violated: stato must not be null.");
        if (iscrizioni == null)
            throw new IllegalStateException("Invariant violated: iscrizioni list must not be null.");
        if (storiaStati == null)
            throw new IllegalStateException("Invariant violated: storiaStati map must not be null.");
    }

    // ---- existing getters/setters ----

    public Categoria getCategoria()                   { return categoria; }
    public Map<String, String> getValoriCampi()       { return Collections.unmodifiableMap(valoriCampi); }
    public void putAllValoriCampi(Map<String, String> valori) { valoriCampi.putAll(valori); }
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
     *
     * @pre  stato != null
     * @pre  data != null
     * @post getStato() == stato
     * @post getStoriaStati().containsKey(stato)
     * @throws IllegalArgumentException if stato or data is null
     */
    public void setStato(StatoProposta stato, LocalDate data)
    {
        if (stato == null)
            throw new IllegalArgumentException("Stato non può essere null.");
        if (data == null)
            throw new IllegalArgumentException("Data non può essere null.");

        if (!this.stato.canTransitionTo(stato))
            throw new IllegalStateException("Transizione non valida: " + this.stato + " → " + stato + ".");

        this.stato = stato;
        storiaStati.put(stato, data);
        checkInvariant();
    }

    // ---- NEW V3 methods ----

    public List<Iscrizione> getIscrizioni()
    {
        return Collections.unmodifiableList(iscrizioni);
    }

    public void addIscrizione(Iscrizione i)
    {
        if (stato != StatoProposta.APERTA)
            throw new IllegalStateException("Iscrizioni consentite solo su proposte APERTE (stato: " + stato + ").");
        iscrizioni.add(i);
    }

    public boolean removeIscrizione(String usernameFruitore)
    {
        if (stato != StatoProposta.APERTA)
            throw new IllegalStateException("Disdetta consentita solo su proposte APERTE (stato: " + stato + ").");
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

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        if (iscrizioni  == null) iscrizioni  = new ArrayList<>();
        if (storiaStati == null) storiaStati = new LinkedHashMap<>();
        if (valoriCampi == null) valoriCampi = new HashMap<>();
    }
}