package it.unibs.ingsoft.v1.persistence.api;

import it.unibs.ingsoft.v1.domain.Catalogo;
import it.unibs.ingsoft.v1.persistence.impl.FileCatalogoRepository;

/**
 * Repository abstraction for the field/category catalogue.
 * Services depend on this interface rather than on the concrete
 * {@link FileCatalogoRepository}, satisfying DIP.
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
