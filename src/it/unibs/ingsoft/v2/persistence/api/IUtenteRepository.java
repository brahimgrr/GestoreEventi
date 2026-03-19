package it.unibs.ingsoft.v2.persistence.api;

import it.unibs.ingsoft.v2.persistence.dto.UtenteData;

public interface IUtenteRepository
{
    UtenteData load();
    void save(UtenteData data);
}
