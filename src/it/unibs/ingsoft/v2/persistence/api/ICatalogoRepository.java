package it.unibs.ingsoft.v2.persistence.api;

import it.unibs.ingsoft.v2.domain.Catalogo;

/**
 * Repository abstraction for the field/category catalogue.
 * A different implementation (JSON, in-memory for tests) can be
 * substituted without touching any service class.
 */
public interface ICatalogoRepository {
    /**
     * Loads the catalogue from persistent storage, or returns a fresh
     *
     * @return the loaded or freshly-created catalogue; never {@code null}
     */
    Catalogo get();

    /**
     * Persists the given catalogue snapshot.
     */
    void save();

}
