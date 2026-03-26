package it.unibs.ingsoft.v3.persistence.impl;

import it.unibs.ingsoft.v3.domain.ArchivioNotifiche;
import it.unibs.ingsoft.v3.domain.SpazioPersonale;
import it.unibs.ingsoft.v3.persistence.api.ISpazioPersonaleRepository;

import java.nio.file.Path;

public class FileSpazioPersonaleRepository extends AbstractFileRepository<ArchivioNotifiche> implements ISpazioPersonaleRepository {

    private ArchivioNotifiche cached;

    public FileSpazioPersonaleRepository(Path path) {
        super(path, ArchivioNotifiche.class, ArchivioNotifiche::new);
    }

    private ArchivioNotifiche getArchivio() {
        if (cached == null) {
            cached = load();
        }
        return cached;
    }

    @Override
    public SpazioPersonale get(String username) {
        return getArchivio().getSpazioDi(username);
    }

    @Override
    public void save() {
        if (cached != null) {
            super.save(cached);
        }
    }
}
