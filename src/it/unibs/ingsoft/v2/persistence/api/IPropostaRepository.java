package it.unibs.ingsoft.v2.persistence.api;

import it.unibs.ingsoft.v2.persistence.dto.PropostaData;

public interface IPropostaRepository
{
    PropostaData load();
    void save(PropostaData data);
}
