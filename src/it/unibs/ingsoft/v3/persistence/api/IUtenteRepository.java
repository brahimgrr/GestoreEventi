package it.unibs.ingsoft.v3.persistence.api;

import it.unibs.ingsoft.v3.persistence.dto.UsersData;

public interface IUtenteRepository
{
    UsersData load();
    void save(UsersData data);
}
