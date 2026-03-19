package it.unibs.ingsoft.v1.persistence.api;

import it.unibs.ingsoft.v1.persistence.impl.FileCategoriaRepository;
import it.unibs.ingsoft.v1.persistence.dto.CatalogoData;

/**
 * Repository abstraction for the field/category catalogue.
 * Services depend on this interface rather than on the concrete
 * {@link FileCategoriaRepository}, satisfying DIP.
 * A different implementation (JSON, in-memory for tests) can be
 * substituted without touching any service class.
 */
public interface ICategoriaRepository
{
    /**
     * Loads the catalogue from persistent storage, or returns a fresh
     * {@link CatalogoData} if no data has been saved yet.
     *
     * @return the loaded or freshly-created catalogue; never {@code null}
     */
    CatalogoData load();

    /**
     * Persists the given catalogue snapshot.
     *
     * @pre  data != null
     * @post the data is durably stored and can be reloaded by {@link #load()}
     */
    void save(CatalogoData data);
}
