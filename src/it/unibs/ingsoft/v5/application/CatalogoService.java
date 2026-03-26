package it.unibs.ingsoft.v5.application;

import it.unibs.ingsoft.v5.domain.*;
import it.unibs.ingsoft.v5.persistence.api.ICatalogoRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Manages base fields, common fields, categories and category-specific fields.
 */
public final class CatalogoService {

    private final ICatalogoRepository repo;

    public CatalogoService(ICatalogoRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    private Catalogo catalogo() {
        return repo.get();
    }

    public boolean nomeEsistente(String nome) {
        return catalogo().nomeEsistenteGlobale(nome);
    }

    // ---------------- CAMPI BASE ----------------

    public void initiateCampiBase() {
        catalogo().fissareCampiBase(
                Arrays.stream(CampoBaseDefinito.values())
                        .map(CampoBaseDefinito::toCampo)
                        .collect(Collectors.toList()),
                null
        );
        repo.save();
    }

    public void addCampiBaseConExtra(List<String> nomi, List<TipoDato> tipi) {
        List<Campo> extra = IntStream.range(0, nomi.size())
                .mapToObj(i -> new Campo(
                        nomi.get(i).trim(),
                        TipoCampo.BASE,
                        tipi.get(i),
                        true))
                .collect(Collectors.toList());

        catalogo().fissareCampiBase(
                Arrays.stream(CampoBaseDefinito.values())
                        .map(CampoBaseDefinito::toCampo)
                        .collect(Collectors.toList()),
                extra
        );
        repo.save();
    }

    public List<Campo> getCampiBase() {
        return catalogo().getCampiBase();
    }

    // ---------------- CAMPI COMUNI ----------------

    public void addCampoComune(String nome, TipoDato tipo, boolean obbligatorio) {
        catalogo().addCampoComune(
                new Campo(nome.trim(), TipoCampo.COMUNE, tipo, obbligatorio)
        );
        repo.save();
    }

    public boolean removeCampoComune(String nome) {
        boolean changed = catalogo().removeCampoComune(nome);
        if (changed) repo.save();
        return changed;
    }

    public boolean setObbligatorietaCampoComune(String nome, boolean obbligatorio) {
        boolean changed = catalogo().updateCampoComune(nome, obbligatorio);
        if (changed) repo.save();
        return changed;
    }

    public List<Campo> getCampiComuni() {
        return catalogo().getCampiComuni();
    }

    // ---------------- CATEGORIE ----------------

    public Categoria createCategoria(String nome) {
        Categoria c = catalogo().addCategoria(nome);
        repo.save();
        return c;
    }

    public boolean removeCategoria(String nome) {
        boolean changed = catalogo().removeCategoria(nome);
        if (changed) repo.save();
        return changed;
    }

    public List<Categoria> getCategorie() {
        return catalogo().getCategorie();
    }

    // ---------------- CAMPI SPECIFICI ----------------

    public void addCampoSpecifico(String categoria, String nome, TipoDato tipo, boolean obbligatorio) {
        catalogo().addCampoSpecifico(
                categoria,
                new Campo(nome.trim(), TipoCampo.SPECIFICO, tipo, obbligatorio)
        );
        repo.save();
    }

    public boolean removeCampoSpecifico(String categoria, String nome) {
        boolean changed = catalogo().removeCampoSpecifico(categoria, nome);
        if (changed) repo.save();
        return changed;
    }

    public boolean setObbligatorietaCampoSpecifico(String categoria, String nome, boolean obbligatorio) {
        boolean changed = catalogo().updateCampoSpecifico(categoria, nome, obbligatorio);
        if (changed) repo.save();
        return changed;
    }
}
