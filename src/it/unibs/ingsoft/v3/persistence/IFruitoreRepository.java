package it.unibs.ingsoft.v3.persistence;

public interface IFruitoreRepository
{
    FruitoreData load();
    void save(FruitoreData data);
}
