package it.unibs.ingsoft.v2.persistence.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.unibs.ingsoft.v2.domain.Proposta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JSON-serializable collection of published (APERTA) proposals.
 */
public final class PropostaData
{
    private final List<Proposta> proposte;

    public PropostaData()
    {
        this.proposte = new ArrayList<>();
    }

    /** Jackson deserialisation factory. */
    @JsonCreator
    public static PropostaData fromJson(
            @JsonProperty("proposte") List<Proposta> proposte)
    {
        PropostaData d = new PropostaData();
        if (proposte != null) d.proposte.addAll(proposte);
        return d;
    }

    public List<Proposta> getProposte()
    {
        return Collections.unmodifiableList(proposte);
    }

    public void addProposta(Proposta p)
    {
        proposte.add(p);
    }
}
