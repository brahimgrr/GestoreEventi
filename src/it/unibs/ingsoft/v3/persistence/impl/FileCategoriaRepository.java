package it.unibs.ingsoft.v3.persistence.impl;

import it.unibs.ingsoft.v3.persistence.api.ICategoriaRepository;
import it.unibs.ingsoft.v3.domain.Catalogo;

import java.nio.file.Path;

public final class FileCategoriaRepository
        extends AbstractFileRepository<Catalogo>
        implements ICategoriaRepository
{
    public FileCategoriaRepository(Path path) { super(path, Catalogo.class, Catalogo::new); }
}
