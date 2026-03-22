package it.unibs.ingsoft.v2.persistence.api;

import it.unibs.ingsoft.v2.persistence.dto.Bacheca;

public interface IBachecaRepository
{
    Bacheca load();
    void save(Bacheca data);
}
