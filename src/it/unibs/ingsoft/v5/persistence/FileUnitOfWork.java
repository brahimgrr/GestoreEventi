package it.unibs.ingsoft.v5.persistence;

import java.io.*;
import java.util.Objects;

/**
 * File-based implementation of {@link IUnitOfWork}.
 * Uses Java serialization for deep-copy snapshots of CatalogoData and PropostaData.
 * On rollback, restores the in-memory objects to their pre-begin() state.
 */
public final class FileUnitOfWork implements IUnitOfWork {
    private final ICategoriaRepository catRepo;
    private final CatalogoData         catalogo;
    private final IPropostaRepository  propRepo;
    private final PropostaData         proposte;

    private byte[] catalogoSnapshot;
    private byte[] proposteSnapshot;

    public FileUnitOfWork(ICategoriaRepository catRepo, CatalogoData catalogo,
                          IPropostaRepository propRepo, PropostaData proposte) {
        this.catRepo  = Objects.requireNonNull(catRepo);
        this.catalogo = Objects.requireNonNull(catalogo);
        this.propRepo = Objects.requireNonNull(propRepo);
        this.proposte = Objects.requireNonNull(proposte);
    }

    @Override
    public void begin() {
        catalogoSnapshot = serialize(catalogo);
        proposteSnapshot = serialize(proposte);
    }

    @Override
    public void commit() {
        catRepo.save(catalogo);
        propRepo.save(proposte);
        catalogoSnapshot = null;
        proposteSnapshot = null;
    }

    @Override
    public void rollback() {
        if (catalogoSnapshot != null) {
            CatalogoData restored = deserialize(catalogoSnapshot, CatalogoData.class);
            catalogo.restoreFrom(restored);
        }
        if (proposteSnapshot != null) {
            PropostaData restored = deserialize(proposteSnapshot, PropostaData.class);
            proposte.restoreFrom(restored);
        }
        catalogoSnapshot = null;
        proposteSnapshot = null;
    }

    private static byte[] serialize(Serializable obj) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Serialization failed during UoW begin()", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T deserialize(byte[] data, Class<T> type) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Deserialization failed during UoW rollback()", e);
        }
    }
}
