package it.unibs.ingsoft.v3.persistence.api;

import it.unibs.ingsoft.v3.domain.Catalogo;

public interface ICategoriaRepository
{
    Catalogo load();
    void save(Catalogo data);
}
