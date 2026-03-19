package it.unibs.ingsoft.v4.persistence;

import java.nio.file.Path;

public final class FileNotificaRepository
        extends AbstractFileRepository<NotificaData>
        implements INotificaRepository
{
    public FileNotificaRepository(Path path) { super(path, NotificaData::new); }
}
