package it.unibs.ingsoft.v3.persistence.impl;

import it.unibs.ingsoft.v3.persistence.api.INotificaRepository;
import it.unibs.ingsoft.v3.persistence.dto.NotificaData;

import java.nio.file.Path;

public final class FileNotificaRepository
        extends AbstractFileRepository<NotificaData>
        implements INotificaRepository
{
    public FileNotificaRepository(Path path) { super(path, NotificaData.class, NotificaData::new); }
}
