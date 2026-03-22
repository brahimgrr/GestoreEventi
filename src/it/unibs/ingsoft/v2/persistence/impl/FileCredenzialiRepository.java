package it.unibs.ingsoft.v2.persistence.impl;

import it.unibs.ingsoft.v2.persistence.api.ICredenzialiRepository;
import it.unibs.ingsoft.v2.persistence.dto.Credenziali;

import java.nio.file.Path;

public final class FileCredenzialiRepository
        extends AbstractFileRepository<Credenziali>
        implements ICredenzialiRepository
{
    public FileCredenzialiRepository(Path path)
    {
        super(path, Credenziali.class, Credenziali::new);
    }
}
