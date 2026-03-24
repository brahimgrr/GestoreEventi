package it.unibs.ingsoft.v5.persistence.api;

import it.unibs.ingsoft.v5.domain.Credenziali;

/**
 * Repository abstraction for user credentials.
 * Satisfies DIP: {@link it.unibs.ingsoft.v1.application.AuthenticationService}
 * depends on this interface, not on the concrete file-based implementation.
 */
public interface ICredenzialiRepository
{
    /**
     * Loads user data from persistent storage, or returns a fresh
     * {@link Credenziali} if no data has been saved yet.
     *
     * @return the loaded or freshly-created user data; never {@code null}
     */
    Credenziali get();

    /**
     * Persists the given user data snapshot.
     *
     * @pre  data != null
     */
    void save();
}
