package it.unibs.ingsoft.v3.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * JSON-serializable DTO for proposals (all states: BOZZA through CONCLUSA).
 */
public final class Bacheca
{
    private final List<Proposta> proposte = new ArrayList<>();

    public Bacheca() {}

    /** Jackson deserialisation factory. */
    @JsonCreator
    public static Bacheca fromJson(@JsonProperty("proposte") List<Proposta> proposte)
    {
        Bacheca d = new Bacheca();
        if (proposte != null) d.proposte.addAll(proposte);
        return d;
    }

    public List<Proposta> getProposte() { return Collections.unmodifiableList(proposte); }
    public void addProposta(Proposta p) { proposte.add(p); }
}
