package it.unibs.ingsoft.v5.persistence.api;

import it.unibs.ingsoft.v5.domain.Bacheca;

public interface IBachecaRepository
{
    Bacheca get();
    void save();
}
