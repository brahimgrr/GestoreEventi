package it.unibs.ingsoft.v4.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.*;

/**
 * Represents an event proposal.
 *
 * <p>Lifecycle: BOZZA → VALIDA → APERTA → CONFERMATA → CONCLUSA
 *                                         → ANNULLATA</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Proposta
{
    private final List<Campo> campiBase;
    private final List<Campo> campiComuni;
    private final Categoria categoria;
    private final Map<String, String>   valoriCampi;
    private StatoProposta stato;
    private LocalDate                   dataPubblicazione;
    private LocalDate                   termineIscrizione;
    private LocalDate                   dataEvento;
    private final List<String>          listaAderenti;
    private final List<PropostaStateChange> stateHistory;

    /**
     * Creates a new draft proposal.
     *
     * @pre categoria != null
     */
    public Proposta(Categoria categoria, List<Campo> campiBase, List<Campo> campiComuni)
    {
        if (categoria == null)
            throw new IllegalArgumentException("La categoria non può essere null.");
        this.categoria = new Categoria(categoria);
        this.campiBase = campiBase == null
                ? new ArrayList<>()
                : campiBase.stream().map(Campo::new).toList();
        this.campiComuni = campiComuni == null
                ? new ArrayList<>()
                : campiComuni.stream().map(Campo::new).toList();
        this.valoriCampi = new LinkedHashMap<>();
        this.listaAderenti = new ArrayList<>();
        this.stateHistory = new ArrayList<>();
        this.stato       = StatoProposta.BOZZA;
        this.stateHistory.add(new PropostaStateChange(StatoProposta.BOZZA, LocalDate.now(AppConstants.clock)));
    }

    /** Jackson deserialisation factory — reconstructs a fully populated (published) proposal. */
    @JsonCreator
    public static Proposta fromJson(
            @JsonProperty("campiBase")         List<Campo>           campiBase,
            @JsonProperty("campiComuni")         List<Campo>           campiComuni,
            @JsonProperty("categoria") Categoria categoria,
            @JsonProperty("valoriCampi")       Map<String, String> valoriCampi,
            @JsonProperty("stato") StatoProposta stato,
            @JsonProperty("dataPubblicazione") LocalDate           dataPubblicazione,
            @JsonProperty("termineIscrizione") LocalDate           termineIscrizione,
            @JsonProperty("dataEvento")        LocalDate           dataEvento,
            @JsonProperty("listaAderenti")     List<String>        listaAderenti,
            @JsonProperty("stateHistory")      List<PropostaStateChange> stateHistory)
    {
        Proposta p = new Proposta(categoria, campiBase, campiComuni);
        if (valoriCampi != null)      p.valoriCampi.putAll(valoriCampi);
        if (stato != null)            p.stato             = stato;
        if (dataPubblicazione != null) p.dataPubblicazione = dataPubblicazione;
        if (termineIscrizione != null) p.termineIscrizione = termineIscrizione;
        if (dataEvento != null)        p.dataEvento        = dataEvento;
        
        p.listaAderenti.clear();
        if (listaAderenti != null)     p.listaAderenti.addAll(listaAderenti);
        
        if (stateHistory != null && !stateHistory.isEmpty()) {
            p.stateHistory.clear();
            p.stateHistory.addAll(stateHistory);
        }
        return p;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    @JsonIgnore
    public List<Campo> getCampi() {
        List<Campo> campiProposta = new ArrayList<>();
        campiProposta.addAll(campiBase);
        campiProposta.addAll(campiComuni);
        campiProposta.addAll(categoria.getCampiSpecifici());
        return campiProposta;
    }

    public StatoProposta getStato() {
        return stato;
    }

    public LocalDate getDataPubblicazione() {
        return dataPubblicazione;
    }

    public LocalDate getTermineIscrizione() {
        return termineIscrizione;
    }

    public LocalDate getDataEvento() {
        return dataEvento;
    }

    public Map<String, String> getValoriCampi() {
        return Collections.unmodifiableMap(valoriCampi);
    }

    public List<String> getListaAderenti() {
        return Collections.unmodifiableList(listaAderenti);
    }
    
    public void addAderente(String username) {
        if (stato != StatoProposta.APERTA)
            throw new IllegalStateException("Impossibile aggiungere aderenti: la proposta non è APERTA.");
        if (!listaAderenti.contains(username)) {
            listaAderenti.add(username);
        }
    }

    /**
     * Resets state to BOZZA without recording the change in stateHistory.
     * Used only during proposal validation to avoid polluting the history
     * with pre-publication BOZZA/VALIDA cycles.
     */
    public void revertToBozzaSilent() {
        if (this.stato == StatoProposta.VALIDA) {
            this.stato = StatoProposta.BOZZA;
        }
    }

    public List<PropostaStateChange> getStateHistory() {
        return Collections.unmodifiableList(stateHistory);
    }

    @JsonIgnore
    public int getNumeroPartecipanti() {
        try {
            return Integer.parseInt(valoriCampi.getOrDefault(AppConstants.CAMPO_NUM_PARTECIPANTI, "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }


    public void putAllValoriCampi(Map<String, String> valori)
    {
        valoriCampi.putAll(valori);

        // Smart Reordering: maintain campiBase -> campiComuni -> campiSpecifici order
        Map<String, String> temp = new HashMap<>(valoriCampi);
        valoriCampi.clear();

        for (Campo c : getCampi()) {
            String nomeCampo = c.getNome();
            if (temp.containsKey(nomeCampo)) {
                valoriCampi.put(nomeCampo, temp.remove(nomeCampo));
            }
        }

        // Add any remaining legacy/extra fields at the very end
        valoriCampi.putAll(temp);
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
        this.stateHistory.add(new PropostaStateChange(next, LocalDate.now(AppConstants.clock)));
    }
}
