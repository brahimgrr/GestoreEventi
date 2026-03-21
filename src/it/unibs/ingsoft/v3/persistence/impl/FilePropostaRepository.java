package it.unibs.ingsoft.v3.persistence.impl;

import it.unibs.ingsoft.v3.persistence.api.IPropostaRepository;
import it.unibs.ingsoft.v3.domain.Bacheca;

import java.nio.file.Path;

public final class FilePropostaRepository
        extends AbstractFileRepository<Bacheca>
        implements IPropostaRepository
{
    public FilePropostaRepository(Path path) { super(path, Bacheca.class, Bacheca::new); }
}
