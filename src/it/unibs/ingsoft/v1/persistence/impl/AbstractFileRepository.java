package it.unibs.ingsoft.v1.persistence.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.nio.file.*;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Generic base class for file-backed JSON repositories.
 * Encapsulates all I/O logic (load + atomic save) so concrete sub-classes
 * need only declare their type parameter and supply a default-value factory.
 *
 * <p>JSON is used instead of Java serialization because:
 * <ul>
 *   <li>Human-readable and debuggable</li>
 *   <li>Schema evolution is safe: unknown fields are ignored by default</li>
 *   <li>No {@code serialVersionUID} fragility</li>
 * </ul>
 */
abstract class AbstractFileRepository<T>
{
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final Path        path;
    private final Class<T>    type;
    private final Supplier<T> defaultValue;

    protected AbstractFileRepository(Path path, Class<T> type, Supplier<T> defaultValue)
    {
        this.path         = Objects.requireNonNull(path);
        this.type         = Objects.requireNonNull(type);
        this.defaultValue = Objects.requireNonNull(defaultValue);
    }

    public T load()
    {
        if (!Files.exists(path))
            return defaultValue.get();

        try
        {
            return MAPPER.readValue(path.toFile(), type);
        }
        catch (IOException e)
        {
            System.err.println("[WARN] Failed to load " + path + ": " + e.getMessage()
                    + " — starting with empty state.");
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
            MAPPER.writeValue(tmp.toFile(), data);
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING,
                                   StandardCopyOption.ATOMIC_MOVE);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException("Failed to save to " + path, e);
        }
    }
}
