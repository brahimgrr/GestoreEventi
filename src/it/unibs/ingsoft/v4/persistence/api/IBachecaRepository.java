package it.unibs.ingsoft.v4.persistence.api;

import it.unibs.ingsoft.v4.domain.Bacheca;

public interface IBachecaRepository {
    Bacheca get();

    void save();
}
