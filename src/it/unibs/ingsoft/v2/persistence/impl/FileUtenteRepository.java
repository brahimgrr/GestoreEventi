package it.unibs.ingsoft.v2.persistence.impl;

import it.unibs.ingsoft.v2.persistence.api.IUtenteRepository;
import it.unibs.ingsoft.v2.persistence.dto.UsersData;

import java.nio.file.Path;

public final class FileUtenteRepository
        extends AbstractFileRepository<UsersData>
        implements IUtenteRepository
{
    public FileUtenteRepository(Path path)
    {
        super(path, UsersData.class, UsersData::new);
    }
}
