package it.unibs.ingsoft.v2.persistence.impl;

import it.unibs.ingsoft.v2.persistence.api.IUtenteRepository;
import it.unibs.ingsoft.v2.persistence.dto.UtenteData;

import java.nio.file.Path;

public final class FileUtenteRepository
        extends AbstractFileRepository<UtenteData>
        implements IUtenteRepository
{
    public FileUtenteRepository(Path path)
    {
        super(path, UtenteData.class, UtenteData::new);
    }
}
