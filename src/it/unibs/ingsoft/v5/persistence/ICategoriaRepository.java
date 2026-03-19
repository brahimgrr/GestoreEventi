package it.unibs.ingsoft.v5.persistence;

/**
 * Repository interface for the catalogue (base fields, common fields, categories).
 */
public interface ICategoriaRepository
{
    /**
     * Loads the catalogue from persistent storage.
     * Returns an empty {@link CatalogoData} if no data exists yet.
     *
     * @return non-null CatalogoData
     */
    CatalogoData load();

    /**
     * Persists the given catalogue snapshot.
     *
     * @pre catalogo != null
     */
    void save(CatalogoData catalogo);
}
