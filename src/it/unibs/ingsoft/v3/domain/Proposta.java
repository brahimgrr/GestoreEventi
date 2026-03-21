package it.unibs.ingsoft.v3.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.*;

public class Proposta
{
    private Categoria                    categoria;
    private Map<String, String>          valoriCampi;
    private StatoProposta                stato;
    private LocalDate                    dataPubblicazione;
    private LocalDate                    termineIscrizione;
    private LocalDate                    dataEvento;
    private LocalDate                    dataConclus;
    private List<Iscrizione>             iscrizioni;
    private Map<StatoProposta, LocalDate> storiaStati;

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
        this.categoria    = categoria;
        this.valoriCampi  = new HashMap<>();
        this.stato        = StatoProposta.BOZZA;
        this.iscrizioni   = new ArrayList<>();
        this.storiaStati  = new LinkedHashMap<>();
        this.storiaStati.put(StatoProposta.BOZZA, LocalDate.now());
        checkInvariant();
    }

    /** No-arg constructor for use by the Jackson factory method only. */
    private Proposta()
    {
        this.valoriCampi = new HashMap<>();
        this.iscrizioni  = new ArrayList<>();
        this.storiaStati = new LinkedHashMap<>();
    }

    /** Jackson deserialisation factory — restores all fields without going through the state machine. */
    @JsonCreator
    public static Proposta fromJson(
            @JsonProperty("categoria")         Categoria                    categoria,
            @JsonProperty("valoriCampi")       Map<String, String>          valoriCampi,
            @JsonProperty("stato")             StatoProposta                stato,
            @JsonProperty("dataPubblicazione") LocalDate                    dataPubblicazione,
            @JsonProperty("termineIscrizione") LocalDate                    termineIscrizione,
            @JsonProperty("dataEvento")        LocalDate                    dataEvento,
            @JsonProperty("dataConclus")       LocalDate                    dataConclus,
            @JsonProperty("iscrizioni")        List<Iscrizione>             iscrizioni,
            @JsonProperty("storiaStati")       Map<StatoProposta, LocalDate> storiaStati)
    {
        if (categoria == null)
            throw new IllegalArgumentException("La categoria non può essere null.");
        Proposta p = new Proposta();
        p.categoria         = categoria;
        p.valoriCampi       = valoriCampi  != null ? new HashMap<>(valoriCampi)      : new HashMap<>();
        p.stato             = stato        != null ? stato                            : StatoProposta.BOZZA;
        p.dataPubblicazione = dataPubblicazione;
        p.termineIscrizione = termineIscrizione;
        p.dataEvento        = dataEvento;
        p.dataConclus       = dataConclus;
        p.iscrizioni        = iscrizioni   != null ? new ArrayList<>(iscrizioni)     : new ArrayList<>();
        p.storiaStati       = storiaStati  != null ? new LinkedHashMap<>(storiaStati) : new LinkedHashMap<>();
        p.checkInvariant();
        return p;
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

    // ---- getters ----

    public Categoria                     getCategoria()          { return categoria; }
    public Map<String, String>           getValoriCampi()        { return Collections.unmodifiableMap(valoriCampi); }
    public StatoProposta                 getStato()              { return stato; }
    public LocalDate                     getDataPubblicazione()  { return dataPubblicazione; }
    public LocalDate                     getTermineIscrizione()  { return termineIscrizione; }
    public LocalDate                     getDataEvento()         { return dataEvento; }
    public LocalDate                     getDataConclus()        { return dataConclus; }
    public List<Iscrizione>              getIscrizioni()         { return Collections.unmodifiableList(iscrizioni); }
    public Map<StatoProposta, LocalDate> getStoriaStati()        { return Collections.unmodifiableMap(storiaStati); }

    public void putAllValoriCampi(Map<String, String> valori) { valoriCampi.putAll(valori); }
    public void setDataPubblicazione(LocalDate d)             { this.dataPubblicazione = d; }
    public void setTermineIscrizione(LocalDate d)             { this.termineIscrizione = d; }
    public void setDataEvento(LocalDate d)                    { this.dataEvento = d; }
    public void setDataConclus(LocalDate d)                   { this.dataConclus = d; }

    /**
     * Changes state and records the transition date in the history.
     *
     * @pre  stato != null
     * @pre  data  != null
     * @throws IllegalArgumentException  if stato or data is null
     * @throws IllegalStateException     if the transition is not allowed by the state machine
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

    public void addIscrizione(Iscrizione i)
    {
        if (stato != StatoProposta.APERTA)
            throw new IllegalStateException("Iscrizioni consentite solo su proposte APERTE (stato: " + stato + ").");
        iscrizioni.add(i);
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
}
