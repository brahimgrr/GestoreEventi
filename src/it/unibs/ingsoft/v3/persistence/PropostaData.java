package it.unibs.ingsoft.v3.persistence;

import it.unibs.ingsoft.v3.model.Proposta;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * Pure serializable DTO for published proposals.
 */
public final class PropostaData implements Serializable
{
    @Serial private static final long serialVersionUID = 1L;

    private final List<Proposta> proposte = new ArrayList<>();

    public List<Proposta> getProposte()
    {
        return Collections.unmodifiableList(proposte);
    }

    public void addProposta(Proposta p)
    {
        proposte.add(p);
    }
}
