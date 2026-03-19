package it.unibs.ingsoft.v3.persistence;

public interface IUtenteRepository
{
    UtenteData load();
    void save(UtenteData data);
}
