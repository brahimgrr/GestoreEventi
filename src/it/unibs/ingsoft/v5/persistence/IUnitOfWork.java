package it.unibs.ingsoft.v5.persistence;

public interface IUnitOfWork {
    /**
     * Begins the unit of work: takes a snapshot of current state for rollback.
     */
    void begin();

    /**
     * Commits: saves both catalogo and proposte to disk.
     */
    void commit();

    /**
     * Rolls back: restores the in-memory state to the snapshot taken at begin().
     */
    void rollback();
}
