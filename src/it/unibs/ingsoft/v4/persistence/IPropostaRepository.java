package it.unibs.ingsoft.v4.persistence;

/**
 * Repository interface for proposals.
 */
public interface IPropostaRepository
{
    /**
     * Loads proposal data from persistent storage.
     * Returns an empty {@link PropostaData} if no data exists yet.
     *
     * @return non-null PropostaData
     */
    PropostaData load();

    /**
     * Persists the given proposal data snapshot.
     *
     * @pre proposteData != null
     */
    void save(PropostaData proposteData);
}
