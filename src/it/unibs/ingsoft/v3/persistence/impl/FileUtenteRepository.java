package it.unibs.ingsoft.v3.persistence.impl;

import it.unibs.ingsoft.v3.persistence.api.IUtenteRepository;
import it.unibs.ingsoft.v3.domain.Credenziali;

import java.nio.file.Path;

public final class FileUtenteRepository
        extends AbstractFileRepository<Credenziali>
        implements IUtenteRepository
{
    public FileUtenteRepository(Path path) { super(path, Credenziali.class, Credenziali::new); }
}
