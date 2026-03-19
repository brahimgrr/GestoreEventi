package it.unibs.ingsoft.v2.persistence.impl;

import it.unibs.ingsoft.v2.persistence.api.IPropostaRepository;
import it.unibs.ingsoft.v2.persistence.dto.PropostaData;

import java.nio.file.Path;

public final class FilePropostaRepository
        extends AbstractFileRepository<PropostaData>
        implements IPropostaRepository
{
    public FilePropostaRepository(Path path)
    {
        super(path, PropostaData.class, PropostaData::new);
    }
}
