package it.unibs.ingsoft.v3.persistence.impl;

import it.unibs.ingsoft.v3.persistence.api.INotificaRepository;
import it.unibs.ingsoft.v3.domain.ArchivioNotifiche;

import java.nio.file.Path;

public final class FileNotificaRepository
        extends AbstractFileRepository<ArchivioNotifiche>
        implements INotificaRepository
{
    private ArchivioNotifiche cached;

    public FileNotificaRepository(Path path) {
        super(path, ArchivioNotifiche.class, ArchivioNotifiche::new);
    }

    @Override
    public ArchivioNotifiche get() {
        if (cached == null) {
            cached = this.get();
        }
        return cached;
    }

    @Override
    public void save() {
        if (cached != null) {
            super.save(cached);
        }
    }
}
