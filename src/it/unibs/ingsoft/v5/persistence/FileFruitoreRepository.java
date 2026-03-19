package it.unibs.ingsoft.v5.persistence;

import java.nio.file.Path;

public final class FileFruitoreRepository
        extends AbstractFileRepository<FruitoreData>
        implements IFruitoreRepository
{
    public FileFruitoreRepository(Path path) { super(path, FruitoreData::new); }
}
