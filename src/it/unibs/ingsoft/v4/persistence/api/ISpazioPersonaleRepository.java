package it.unibs.ingsoft.v4.persistence.api;

import it.unibs.ingsoft.v4.domain.SpazioPersonale;

public interface ISpazioPersonaleRepository {
    /**
     * Retrieves the personal space (notifications) for a given user.
     * @param username the username of the Fruitore
     * @return the SpazioPersonale object
     */
    SpazioPersonale get(String username);

    /**
     * Saves the personal space data to persistence.
     */
    void save();
}
