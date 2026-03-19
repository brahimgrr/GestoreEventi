package it.unibs.ingsoft.v3.service;

import it.unibs.ingsoft.v3.model.*;
import it.unibs.ingsoft.v3.persistence.CatalogoData;
import it.unibs.ingsoft.v3.persistence.ICategoriaRepository;

import java.util.*;

/**
 * Service responsible for category definitions and category-specific fields.
 * System-wide fields (base and common) are managed by {@link CampoService}.
 */
public final class CategoriaService
{
    private final ICategoriaRepository repo;
    private final CatalogoData         catalogo;
    private final CampoService         campoService;

    /**
     * @pre repo         != null
     * @pre catalogo     != null
     * @pre campoService != null
     */
    public CategoriaService(ICategoriaRepository repo, CatalogoData catalogo, CampoService campoService)
    {
        this.repo         = Objects.requireNonNull(repo);
        this.catalogo     = Objects.requireNonNull(catalogo);
        this.campoService = Objects.requireNonNull(campoService);
    }

    public Categoria createCategoria(String nomeCategoria)
    {
        nomeCategoria = normalizza(nomeCategoria);

        if (catalogo.findCategoria(nomeCategoria).isPresent())
            throw new IllegalArgumentException("Categoria già esistente.");

        Categoria cat = new Categoria(nomeCategoria);
        catalogo.addCategoria(cat);
        repo.save(catalogo);
        return cat;
    }

    public boolean removeCategoria(String nomeCategoria)
    {
        final String n = normalizza(nomeCategoria);
        boolean removed = catalogo.removeCategoria(n);
        if (removed) repo.save(catalogo);
        return removed;
    }

    public List<Categoria> getCategorie()
    {
        return catalogo.getCategorie();
    }

    public Categoria getCategoriaOrThrow(String nomeCategoria)
    {
        final String nome = normalizza(nomeCategoria);
        return catalogo.findCategoria(nome)
                       .orElseThrow(() -> new IllegalArgumentException("Categoria non trovata: \"" + nome + "\"."));
    }

    public void addCampoSpecifico(String nomeCategoria, String nomeCampo, TipoDato tipoDato, boolean obbligatorio)
    {
        nomeCampo = normalizza(nomeCampo);

        if (campoService.nomeBaseOComuneEsistente(nomeCampo))
            throw new IllegalArgumentException("Esiste già un campo con il nome: \"" + nomeCampo + "\".");

        Categoria c = getCategoriaOrThrow(nomeCategoria);

        final String nome = nomeCampo;
        if (c.getCampiSpecifici().stream().anyMatch(f -> f.getNome().equalsIgnoreCase(nome)))
            throw new IllegalArgumentException("Esiste già un campo specifico con il nome: \"" + nome + "\" in questa categoria.");

        c.addCampoSpecifico(new Campo(nomeCampo, TipoCampo.SPECIFICO, tipoDato, obbligatorio));
        repo.save(catalogo);
    }

    public boolean removeCampoSpecifico(String nomeCategoria, String nomeCampo)
    {
        nomeCampo = normalizza(nomeCampo);
        Categoria c = getCategoriaOrThrow(nomeCategoria);
        boolean removed = c.removeCampoSpecifico(nomeCampo);
        if (removed) repo.save(catalogo);
        return removed;
    }

    public boolean setObbligatorietaCampoSpecifico(String nomeCategoria, String nomeCampo, boolean obbligatorio)
    {
        nomeCampo = normalizza(nomeCampo);
        Categoria c = getCategoriaOrThrow(nomeCategoria);
        boolean ok = c.setObbligatorietaCampoSpecifico(nomeCampo, obbligatorio);
        if (ok) repo.save(catalogo);
        return ok;
    }

    private String normalizza(String s)
    {
        if (s == null) throw new IllegalArgumentException("Nome non valido (null).");
        return s.trim();
    }
}
