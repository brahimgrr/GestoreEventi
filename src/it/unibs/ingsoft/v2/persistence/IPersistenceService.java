package it.unibs.ingsoft.v2.persistence;

/**
 * Abstraction over the persistence layer.
 * Services depend on this interface rather than on the concrete {@link DatabaseService},
 * satisfying the Dependency Inversion Principle (DIP).
 * A different implementation (JSON file, in-memory store, …) can be substituted
 * without touching any service class.
 */
public interface IPersistenceService
{
    /**
     * Loads the application data from persistent storage, or creates and returns
     * a fresh {@link AppData} instance if no data has been saved yet.
     *
     * @return the loaded or newly-created {@link AppData}; never {@code null}
     */
    AppData loadOrCreate();

    /**
     * Persists the given application data to storage.
     *
     * @pre  data != null
     * @post the data is durably stored and can be reloaded by {@link #loadOrCreate()}
     * @param data the application data snapshot to save
     * @throws java.io.UncheckedIOException if the write fails
     */
    void save(AppData data);
}
