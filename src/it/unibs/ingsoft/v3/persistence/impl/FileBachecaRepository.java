package it.unibs.ingsoft.v3.persistence.impl;

import it.unibs.ingsoft.v3.domain.Bacheca;
import it.unibs.ingsoft.v3.persistence.api.IBachecaRepository;

import java.nio.file.Path;

public final class FileBachecaRepository
        extends AbstractFileRepository<Bacheca>
        implements IBachecaRepository {
    private Bacheca cached;

    public FileBachecaRepository(Path path) {
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
