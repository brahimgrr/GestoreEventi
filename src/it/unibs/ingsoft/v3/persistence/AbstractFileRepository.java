package it.unibs.ingsoft.v3.persistence;

import java.io.*;
import java.nio.file.*;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Generic base class for file-backed serializable repositories.
 * Encapsulates all I/O logic (load + atomic save) so concrete sub-classes
 * need only declare their type parameter and supply a default-value factory.
 */
abstract class AbstractFileRepository<T extends Serializable>
{
    private final Path        path;
    private final Supplier<T> defaultValue;

    protected AbstractFileRepository(Path path, Supplier<T> defaultValue)
    {
        this.path         = Objects.requireNonNull(path);
        this.defaultValue = Objects.requireNonNull(defaultValue);
    }

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
