package it.unibs.ingsoft.v3.persistence.api;

import it.unibs.ingsoft.v3.persistence.dto.CatalogoData;

public interface ICategoriaRepository
{
    CatalogoData load();
    void save(CatalogoData data);
}
