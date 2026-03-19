package it.unibs.ingsoft.v3.persistence;

import java.nio.file.Path;

public final class FilePropostaRepository
        extends AbstractFileRepository<PropostaData>
        implements IPropostaRepository
{
    public FilePropostaRepository(Path path) { super(path, PropostaData::new); }
}
