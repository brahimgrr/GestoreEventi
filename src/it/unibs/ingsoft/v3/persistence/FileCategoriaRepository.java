package it.unibs.ingsoft.v3.persistence;

import java.nio.file.Path;

public final class FileCategoriaRepository
        extends AbstractFileRepository<CatalogoData>
        implements ICategoriaRepository
{
    public FileCategoriaRepository(Path path) { super(path, CatalogoData::new); }
}
