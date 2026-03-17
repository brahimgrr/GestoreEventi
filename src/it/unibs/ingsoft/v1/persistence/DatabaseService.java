package it.unibs.ingsoft.v1.persistence;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class DatabaseService implements IPersistenceService
{
    private final Path storagePath;

    public DatabaseService(Path storagePath)
    {
        this.storagePath = storagePath;
    }

    public synchronized AppData loadOrCreate()
    {
        if (Files.exists(storagePath)) {
            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(storagePath)))
            {
                Object obj = in.readObject();
                return (AppData) obj;

            } catch (Exception e) {
                throw new RuntimeException("Errore nel caricamento dei dati.", e);
            }
        }

        return new AppData();
    }

    public synchronized void save(AppData data)
    {
        try {
            Files.createDirectories(storagePath.getParent());

        } catch (IOException e) {
            throw new UncheckedIOException("Impossibile creare la cartella dati.", e);
        }

        Path tmp = storagePath.resolveSibling(storagePath.getFileName() + ".tmp");

        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(tmp)))
        {
            out.writeObject(data);

        } catch (IOException e) {
            throw new UncheckedIOException("Errore nel salvataggio dei dati.", e);
        }

        try
        {
            Files.move(tmp, storagePath,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);

        } catch (IOException e) {
            throw new UncheckedIOException("Errore nel commit del salvataggio.", e);
        }
    }
}