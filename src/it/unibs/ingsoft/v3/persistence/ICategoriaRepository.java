package it.unibs.ingsoft.v3.persistence;

public interface ICategoriaRepository
{
    CatalogoData load();
    void save(CatalogoData data);
}
