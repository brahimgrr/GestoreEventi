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
public final class Bacheca
{
    private final List<Proposta> proposte;

    public Bacheca()
    {
        this.proposte = new ArrayList<>();
    }

    /** Jackson deserialisation factory. */
    @JsonCreator
    public static Bacheca fromJson(
            @JsonProperty("proposte") List<Proposta> proposte)
    {
        Bacheca bacheca = new Bacheca();
        if (proposte != null) bacheca.proposte.addAll(proposte);
        return bacheca;
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
