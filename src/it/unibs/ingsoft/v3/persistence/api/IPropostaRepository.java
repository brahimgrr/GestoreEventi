package it.unibs.ingsoft.v3.persistence.api;

import it.unibs.ingsoft.v3.persistence.dto.PropostaData;

public interface IPropostaRepository
{
    PropostaData load();
    void save(PropostaData data);
}
