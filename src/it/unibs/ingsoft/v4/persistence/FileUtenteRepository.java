package it.unibs.ingsoft.v4.persistence;

import java.nio.file.Path;

public final class FileUtenteRepository
        extends AbstractFileRepository<UtenteData>
        implements IUtenteRepository
{
    public FileUtenteRepository(Path path) { super(path, UtenteData::new); }
}
