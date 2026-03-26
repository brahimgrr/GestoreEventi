package it.unibs.ingsoft.v3.persistence.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
abstract class AbstractFileRepository<T> {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");


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
                    })
                    .addSerializer(LocalDateTime.class, new StdSerializer<LocalDateTime>(LocalDateTime.class) {
                        @Override
                        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                            gen.writeString(value.format(DATE_TIME_FMT));
                        }
                    })
                    .addDeserializer(LocalDateTime.class, new StdDeserializer<LocalDateTime>(LocalDateTime.class) {
                        @Override
                        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                            return LocalDateTime.parse(p.getValueAsString(), DATE_TIME_FMT);
                        }
                    })
            )
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final Path path;
    private final Class<T> type;
    private final Supplier<T> defaultValue;

    protected AbstractFileRepository(Path path, Class<T> type, Supplier<T> defaultValue) {
        this.path = Objects.requireNonNull(path);
        this.type = Objects.requireNonNull(type);
        this.defaultValue = Objects.requireNonNull(defaultValue);
    }

    public T load() {
        if (!Files.exists(path))
            return defaultValue.get();

        try {
            return MAPPER.readValue(path.toFile(), type);
        } catch (IOException e) {
            System.err.println("[WARN] Impossibile leggere " + path + ": " + e.getMessage()
                    + " — avvio con stato vuoto.");
            return defaultValue.get();
        }
    }

    public void save(T data) {
        Objects.requireNonNull(data);

        try {
            if (path.getParent() != null)
                Files.createDirectories(path.getParent());

            Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
            MAPPER.writeValue(tmp.toFile(), data);
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException("Impossibile salvare i dati in: " + path, e);
        }
    }
}
