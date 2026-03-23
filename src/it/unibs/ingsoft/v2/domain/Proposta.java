package it.unibs.ingsoft.v2.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.*;

/**
 * Represents an event proposal.
 *
 * <p>Lifecycle: BOZZA → VALIDA → APERTA (terminal).
 * Call {@link it.unibs.ingsoft.v2.application.PropostaService#validaProposta}
 * to transition to VALIDA; call
 * to transition to APERTA and persist.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Proposta
{
    private final List<Campo> campiBase;
    private final List<Campo> campiComuni;
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
        this.stato       = StatoProposta.BOZZA;
    }

    /** Jackson deserialisation factory — reconstructs a fully populated (published) proposal. */
    @JsonCreator
    public static Proposta fromJson(
            @JsonProperty("campiBase")         List<Campo>           campiBase,
            @JsonProperty("campiComuni")         List<Campo>           campiComuni,
            @JsonProperty("categoria")         Categoria           categoria,
            @JsonProperty("valoriCampi")       Map<String, String> valoriCampi,
            @JsonProperty("stato")             StatoProposta       stato,
            @JsonProperty("dataPubblicazione") LocalDate           dataPubblicazione,
            @JsonProperty("termineIscrizione") LocalDate           termineIscrizione,
            @JsonProperty("dataEvento")        LocalDate           dataEvento)
    {
        Proposta p = new Proposta(categoria, campiBase, campiComuni);
        if (valoriCampi != null)      p.valoriCampi.putAll(valoriCampi);
        if (stato != null)            p.stato             = stato;
        if (dataPubblicazione != null) p.dataPubblicazione = dataPubblicazione;
        if (termineIscrizione != null) p.termineIscrizione = termineIscrizione;
        if (dataEvento != null)        p.dataEvento        = dataEvento;
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
        return valoriCampi;
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
    }
}
