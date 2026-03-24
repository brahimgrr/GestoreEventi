package it.unibs.ingsoft.v5.support;

import it.unibs.ingsoft.v5.domain.Bacheca;
import it.unibs.ingsoft.v5.persistence.api.IBachecaRepository;

public final class InMemoryBachecaRepository implements IBachecaRepository {

    private final Bacheca bacheca;
    private int saveCount;

    public InMemoryBachecaRepository() {
        this(new Bacheca());
    }

    public InMemoryBachecaRepository(Bacheca bacheca) {
        this.bacheca = bacheca;
    }

    @Override
    public Bacheca get() {
        return bacheca;
    }

    @Override
    public void save() {
        saveCount++;
    }

    public int getSaveCount() {
        return saveCount;
    }
}
