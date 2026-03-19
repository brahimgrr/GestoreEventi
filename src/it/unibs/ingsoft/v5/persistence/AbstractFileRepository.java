package it.unibs.ingsoft.v5.persistence;

import java.io.*;
import java.nio.file.*;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Generic base class for file-backed serializable repositories.
 * Encapsulates all I/O logic (load + atomic save) so concrete sub-classes
 * need only declare their type parameter and supply a default-value factory.
 *
 * Concrete sub-classes:
 *   <pre>
 *   public final class FileCategoriaRepository
 *           extends AbstractFileRepository{@literal <}CatalogoData{@literal >}
 *           implements ICategoriaRepository {
 *       public FileCategoriaRepository(Path path) { super(path, CatalogoData::new); }
 *   }
 *   </pre>
 */
abstract class AbstractFileRepository<T extends Serializable>
{
    private final Path        path;
    private final Supplier<T> defaultValue;

    /**
     * @pre path != null
     * @pre defaultValue != null
     */
    protected AbstractFileRepository(Path path, Supplier<T> defaultValue)
    {
        this.path         = Objects.requireNonNull(path);
        this.defaultValue = Objects.requireNonNull(defaultValue);
    }

    /**
     * Loads data from the file.
     * Returns a fresh default instance if the file does not exist or is corrupt.
     */
    public T load()
    {
        if (!Files.exists(path))
            return defaultValue.get();

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(path))))
        {
            @SuppressWarnings("unchecked")
            T data = (T) ois.readObject();
            return data;
        }
        catch (IOException | ClassNotFoundException e)
        {
            return defaultValue.get();
        }
    }

    /**
     * Saves data atomically: serialises to a {@code .tmp} file then
     * moves it into place with {@link StandardCopyOption#ATOMIC_MOVE}.
     *
     * @throws UncheckedIOException on I/O failure
     */
    public void save(T data)
    {
        Objects.requireNonNull(data);

        try
        {
            if (path.getParent() != null)
                Files.createDirectories(path.getParent());

            Path tmp = path.resolveSibling(path.getFileName() + ".tmp");

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(tmp))))
            {
                oos.writeObject(data);
            }

            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING,
                                   StandardCopyOption.ATOMIC_MOVE);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException("Failed to save to " + path, e);
        }
    }
}
