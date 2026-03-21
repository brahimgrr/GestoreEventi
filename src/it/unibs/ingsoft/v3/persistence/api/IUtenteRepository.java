package it.unibs.ingsoft.v3.persistence.api;

import it.unibs.ingsoft.v3.domain.Credenziali;

public interface IUtenteRepository
{
    Credenziali load();
    void save(Credenziali data);
}
