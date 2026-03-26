package it.unibs.ingsoft.v1.persistence.impl;

import it.unibs.ingsoft.v1.domain.Catalogo;
import it.unibs.ingsoft.v1.persistence.api.ICatalogoRepository;

import java.nio.file.Path;

public final class FileCatalogoRepository
        extends AbstractFileRepository<Catalogo>
        implements ICatalogoRepository {

    private Catalogo cached;

    public FileCatalogoRepository(Path path) {
        super(path, Catalogo.class, Catalogo::new);
    }

    @Override
    public Catalogo get() {
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