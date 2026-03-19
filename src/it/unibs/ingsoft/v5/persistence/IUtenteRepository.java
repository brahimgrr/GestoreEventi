package it.unibs.ingsoft.v5.persistence;

/**
 * Repository interface for configuratore user credentials.
 */
public interface IUtenteRepository
{
    /**
     * Loads user data from persistent storage.
     * Returns an empty {@link UtenteData} if no data exists yet.
     *
     * @return non-null UtenteData
     */
    UtenteData load();

    /**
     * Persists the given user data snapshot.
     *
     * @pre utenti != null
     */
    void save(UtenteData utenti);
}
