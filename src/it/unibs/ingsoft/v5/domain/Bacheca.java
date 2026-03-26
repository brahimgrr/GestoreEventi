package it.unibs.ingsoft.v5.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JSON-serializable collection of proposals.
 */
public final class Bacheca {
    private final List<Proposta> proposte;

    public Bacheca() {
        this.proposte = new ArrayList<>();
    }

    /**
     * Jackson deserialisation factory.
     */
    @JsonCreator
    public static Bacheca fromJson(
            @JsonProperty("proposte") List<Proposta> proposte) {
        Bacheca bacheca = new Bacheca();
        if (proposte != null) bacheca.proposte.addAll(proposte);
        return bacheca;
    }

    public List<Proposta> getProposte() {
        return Collections.unmodifiableList(proposte);
    }

    public void addProposta(Proposta p) {
        proposte.add(p);
    }
}
