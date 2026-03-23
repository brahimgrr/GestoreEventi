package it.unibs.ingsoft.v1.support;

import it.unibs.ingsoft.v1.domain.Catalogo;
import it.unibs.ingsoft.v1.persistence.api.ICatalogoRepository;

public final class InMemoryCatalogoRepository implements ICatalogoRepository {

    private final Catalogo catalogo;
    private int saveCount;

    public InMemoryCatalogoRepository() {
        this(new Catalogo());
    }

    public InMemoryCatalogoRepository(Catalogo catalogo) {
        this.catalogo = catalogo;
    }

    @Override
    public Catalogo get() {
        return catalogo;
    }

    @Override
    public void save() {
        saveCount++;
    }

    public int getSaveCount() {
        return saveCount;
    }
}
