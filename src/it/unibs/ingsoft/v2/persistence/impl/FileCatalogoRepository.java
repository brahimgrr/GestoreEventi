package it.unibs.ingsoft.v2.persistence.impl;

import it.unibs.ingsoft.v2.persistence.api.ICatalogoRepository;
import it.unibs.ingsoft.v2.persistence.dto.Catalogo;

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
