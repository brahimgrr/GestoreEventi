package it.unibs.ingsoft.v1.persistence.impl;

import it.unibs.ingsoft.v1.persistence.api.ICatalogoRepository;
import it.unibs.ingsoft.v1.domain.Catalogo;

import java.nio.file.Path;

public final class FileCatalogoRepository
        extends AbstractFileRepository<Catalogo>
        implements ICatalogoRepository
{
    public FileCatalogoRepository(Path path)
    {
        super(path, Catalogo.class, Catalogo::new);
    }
}
