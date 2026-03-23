package it.unibs.ingsoft.v3.persistence.impl;

import it.unibs.ingsoft.v3.domain.Credenziali;
import it.unibs.ingsoft.v3.persistence.api.ICredenzialiRepository;

import java.nio.file.Path;

public final class FileCredenzialiRepository
        extends AbstractFileRepository<Credenziali>
        implements ICredenzialiRepository
{
    private Credenziali cached;

    public FileCredenzialiRepository(Path path)
    {
        super(path, Credenziali.class, Credenziali::new);
    }

    @Override
    public Credenziali get() {
        if (cached == null) {
            cached = load();
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
