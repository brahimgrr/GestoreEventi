package it.unibs.ingsoft.v1.persistence.api;

import it.unibs.ingsoft.v1.persistence.dto.UsersData;

/**
 * Repository abstraction for user credentials.
 * Satisfies DIP: {@link it.unibs.ingsoft.v1.application.AuthenticationService}
 * depends on this interface, not on the concrete file-based implementation.
 */
public interface IUtenteRepository
{
    /**
     * Loads user data from persistent storage, or returns a fresh
     * {@link UsersData} if no data has been saved yet.
     *
     * @return the loaded or freshly-created user data; never {@code null}
     */
    UsersData load();

    /**
     * Persists the given user data snapshot.
     *
     * @pre  data != null
     * @post the data is durably stored and can be reloaded by {@link #load()}
     */
    void save(UsersData data);
}
