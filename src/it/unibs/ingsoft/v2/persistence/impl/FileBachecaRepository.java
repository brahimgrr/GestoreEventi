package it.unibs.ingsoft.v2.persistence.impl;

import it.unibs.ingsoft.v2.persistence.api.IBachecaRepository;
import it.unibs.ingsoft.v2.persistence.dto.Bacheca;

import java.nio.file.Path;

public final class FileBachecaRepository
        extends AbstractFileRepository<Bacheca>
        implements IBachecaRepository
{
    public FileBachecaRepository(Path path)
    {
        super(path, Bacheca.class, Bacheca::new);
    }
}
