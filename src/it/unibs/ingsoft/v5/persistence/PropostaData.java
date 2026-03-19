package it.unibs.ingsoft.v5.persistence;

import it.unibs.ingsoft.v5.model.Proposta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pure serializable DTO holding the list of all proposals.
 */
public final class PropostaData implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final List<Proposta> proposte = new ArrayList<>();

    public List<Proposta> getProposte()
    {
        return Collections.unmodifiableList(proposte);
    }

    public void addProposta(Proposta p)
    {
        proposte.add(p);
    }

    // Package-private: used only by FileUnitOfWork for rollback
    void restoreFrom(PropostaData src)
    {
        this.proposte.clear();
        this.proposte.addAll(src.proposte);
    }
}
