package it.unibs.ingsoft.v3.persistence.api;

import it.unibs.ingsoft.v3.domain.Bacheca;

public interface IPropostaRepository
{
    Bacheca load();
    void save(Bacheca data);
}
