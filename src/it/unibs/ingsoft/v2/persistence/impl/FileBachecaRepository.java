package it.unibs.ingsoft.v2.persistence.impl;

import it.unibs.ingsoft.v2.persistence.api.IBachecaRepository;
import it.unibs.ingsoft.v2.domain.Bacheca;

import java.nio.file.Path;

public final class FileBachecaRepository
        extends AbstractFileRepository<Bacheca>
        implements IBachecaRepository
{
    private Bacheca cached;

    public FileBachecaRepository(Path path)
    {
        super(path, Bacheca.class, Bacheca::new);
    }

    @Override
    public Bacheca get() {
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
