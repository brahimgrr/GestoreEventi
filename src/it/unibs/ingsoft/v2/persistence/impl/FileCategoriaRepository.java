package it.unibs.ingsoft.v2.persistence.impl;

import it.unibs.ingsoft.v2.persistence.api.ICategoriaRepository;
import it.unibs.ingsoft.v2.persistence.dto.CatalogoData;

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
