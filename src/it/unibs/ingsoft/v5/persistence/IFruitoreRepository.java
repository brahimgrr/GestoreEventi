package it.unibs.ingsoft.v5.persistence;

/**
 * Repository interface for fruitore credentials.
 */
public interface IFruitoreRepository
{
    /**
     * Loads fruitore data from persistent storage.
     * Returns an empty {@link FruitoreData} if no data exists yet.
     *
     * @return non-null FruitoreData
     */
    FruitoreData load();

    /**
     * Persists the given fruitore data snapshot.
     *
     * @pre fData != null
     */
    void save(FruitoreData fData);
}
