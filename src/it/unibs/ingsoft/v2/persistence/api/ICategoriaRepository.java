package it.unibs.ingsoft.v2.persistence.api;

import it.unibs.ingsoft.v2.persistence.dto.CatalogoData;

public interface ICategoriaRepository
{
    CatalogoData load();
    void save(CatalogoData data);
}
