package it.unibs.ingsoft.v2.persistence.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

/**
 * Generic base class for file-backed JSON repositories.
 *
 * <p>Replaces the previous Java-serialization implementation.
 * JSON is used because it is:
 * <ul>
 *   <li>Human-readable and debuggable</li>
 *   <li>Safe across class refactors (no {@code serialVersionUID} fragility)</li>
 *   <li>Forward-compatible: unknown fields are silently ignored</li>
 * </ul>
 *
 * <p>Saves atomically: writes to a {@code .tmp} sibling file then renames,
 * so the target file is never left in a partially-written state.</p>
 *
 * @param <T> the DTO type to serialize / deserialize
 */
abstract class AbstractFileRepository<T>
{
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new SimpleModule()
                .addSerializer(LocalDate.class, new StdSerializer<LocalDate>(LocalDate.class) {
                    @Override
                    public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                        gen.writeString(value.format(DATE_FMT));
                    }
                })
                .addDeserializer(LocalDate.class, new StdDeserializer<LocalDate>(LocalDate.class) {
                    @Override
                    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                        return LocalDate.parse(p.getValueAsString(), DATE_FMT);
                    }
                }))
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final Path        path;
    private final Class<T>    type;
    private final Supplier<T> defaultValue;

    protected AbstractFileRepository(Path path, Class<T> type, Supplier<T> defaultValue)
    {
        this.path         = path;
        this.type         = type;
        this.defaultValue = defaultValue;
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
            System.err.println("[WARN] Impossibile leggere " + path + ": " + e.getMessage()
                    + " — avvio con stato vuoto.");
            return defaultValue.get();
        }
    }

    public void save(T data)
    {
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
            throw new UncheckedIOException("Impossibile salvare i dati in: " + path, e);
        }
    }
}
