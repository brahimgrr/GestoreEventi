package it.unibs.ingsoft.v3.persistence.impl;

import it.unibs.ingsoft.v3.persistence.api.IUtenteRepository;
import it.unibs.ingsoft.v3.persistence.dto.UsersData;

import java.nio.file.Path;

public final class FileUtenteRepository
        extends AbstractFileRepository<UsersData>
        implements IUtenteRepository
{
    public FileUtenteRepository(Path path) { super(path, UsersData.class, UsersData::new); }
}
