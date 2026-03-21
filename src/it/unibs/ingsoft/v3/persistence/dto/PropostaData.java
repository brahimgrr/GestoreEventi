package it.unibs.ingsoft.v3.persistence.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.unibs.ingsoft.v3.domain.Proposta;

import java.util.*;

/**
 * JSON-serializable DTO for proposals (all states: BOZZA through CONCLUSA).
 */
public final class PropostaData
{
    private final List<Proposta> proposte = new ArrayList<>();

    public PropostaData() {}

    /** Jackson deserialisation factory. */
    @JsonCreator
    public static PropostaData fromJson(@JsonProperty("proposte") List<Proposta> proposte)
    {
        PropostaData d = new PropostaData();
        if (proposte != null) d.proposte.addAll(proposte);
        return d;
    }

    public List<Proposta> getProposte() { return Collections.unmodifiableList(proposte); }
    public void addProposta(Proposta p) { proposte.add(p); }
}
