package it.unibs.ingsoft.v3.persistence;

public interface IPropostaRepository
{
    PropostaData load();
    void save(PropostaData data);
}
