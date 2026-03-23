package it.unibs.ingsoft.v1.support;

import it.unibs.ingsoft.v1.domain.Credenziali;
import it.unibs.ingsoft.v1.persistence.api.ICredenzialiRepository;

public final class InMemoryCredenzialiRepository implements ICredenzialiRepository {

    private final Credenziali credenziali;
    private int saveCount;

    public InMemoryCredenzialiRepository() {
        this(new Credenziali());
    }

    public InMemoryCredenzialiRepository(Credenziali credenziali) {
        this.credenziali = credenziali;
    }

    @Override
    public Credenziali get() {
        return credenziali;
    }

    @Override
    public void save() {
        saveCount++;
    }

    public int getSaveCount() {
        return saveCount;
    }
}
