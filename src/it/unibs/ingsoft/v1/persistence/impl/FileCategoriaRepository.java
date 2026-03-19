package it.unibs.ingsoft.v1.persistence.impl;

import it.unibs.ingsoft.v1.persistence.api.ICategoriaRepository;
import it.unibs.ingsoft.v1.persistence.dto.CatalogoData;

import java.nio.file.Path;

public final class FileCategoriaRepository
        extends AbstractFileRepository<CatalogoData>
        implements ICategoriaRepository
{
    public FileCategoriaRepository(Path path)
    {
        super(path, CatalogoData.class, CatalogoData::new);
    }
}
