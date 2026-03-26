package it.unibs.ingsoft.v2.persistence.api;

import it.unibs.ingsoft.v2.domain.Bacheca;

public interface IBachecaRepository {
    Bacheca get();

    void save();
}
