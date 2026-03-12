package it.unibs.ingsoft.v3.persistence;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DatabaseService
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

        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(storagePath)))
        {
            out.writeObject(data);

        } catch (IOException e) {
            throw new UncheckedIOException("Errore nel salvataggio dei dati.", e);
        }
    }
}