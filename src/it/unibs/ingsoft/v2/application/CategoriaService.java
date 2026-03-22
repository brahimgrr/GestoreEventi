package it.unibs.ingsoft.v2.application;

import it.unibs.ingsoft.v2.domain.*;
import it.unibs.ingsoft.v2.persistence.api.ICatalogoRepository;
import it.unibs.ingsoft.v2.persistence.dto.Catalogo;

import java.util.*;

/**
 * Manages category definitions and category-specific fields.
 *
 * <p>Delegates global name-uniqueness checks to {@link CampoService} so that
 * specific field names never clash with base or common field names.</p>
 */
public final class CategoriaService
{
    private final ICatalogoRepository repo;
    private final Catalogo catalogo;
    private final CampoService         campoService;

    public CategoriaService(ICatalogoRepository repo, CampoService campoService)
    {
        this.repo         = Objects.requireNonNull(repo);
        this.campoService = Objects.requireNonNull(campoService);
        this.catalogo     = repo.load();
    }

    // ---------------------------------------------------------------
    // CATEGORIE
    // ---------------------------------------------------------------

    /**
     * Creates a new category.
     *
     * @throws IllegalArgumentException if the name already exists (case-insensitive)
     */
    public void createCategoria(String nome)
    {
        nome = normalizza(nome);
        if (categoriaEsistente(nome))
            throw new IllegalArgumentException("Esiste già una categoria con il nome: \"" + nome + "\".");
        catalogo.addCategoria(new Categoria(nome));
        repo.save(catalogo);
    }

    /**
     * Removes the category with the given name (case-insensitive).
     *
     * @return true if removed, false if not found
     */
    public boolean removeCategoria(String nome)
    {
        boolean removed = catalogo.removeCategoria(normalizza(nome));
        if (removed) repo.save(catalogo);
        return removed;
    }

    public List<Categoria> getCategorie() { return catalogo.getCategorie(); }

    /**
     * Returns the category with the given name.
     *
     * @throws NoSuchElementException if not found
     */
    public Categoria getCategoriaOrThrow(String nome)
    {
        return catalogo.findCategoria(normalizza(nome))
                .orElseThrow(() -> new NoSuchElementException("Categoria non trovata: \"" + nome + "\"."));
    }

    // ---------------------------------------------------------------
    // CAMPI SPECIFICI
    // ---------------------------------------------------------------

    /**
     * Adds a specific field to the named category.
     *
     * @throws IllegalArgumentException if the name conflicts with any existing field
     * @throws NoSuchElementException   if the category does not exist
     */
    public void addCampoSpecifico(String nomeCategoria, String nomeCampo, TipoDato tipoDato, boolean obbligatorio)
    {
        nomeCampo = normalizza(nomeCampo);
        if (campoService.nomeEsiste(nomeCampo))
            throw new IllegalArgumentException(
                    "Esiste già un campo con il nome: \"" + nomeCampo + "\".");

        Categoria cat = getCategoriaOrThrow(nomeCategoria);
        cat.addCampoSpecifico(new Campo(nomeCampo, TipoCampo.SPECIFICO, tipoDato, obbligatorio));
        repo.save(catalogo);
    }

    /**
     * Removes the named specific field from the named category.
     *
     * @return true if removed, false if not found
     */
    public boolean removeCampoSpecifico(String nomeCategoria, String nomeCampo)
    {
        Categoria cat = getCategoriaOrThrow(nomeCategoria);
        boolean removed = cat.removeCampoSpecifico(normalizza(nomeCampo));
        if (removed) repo.save(catalogo);
        return removed;
    }

    /**
     * Changes the mandatory flag of the named specific field.
     *
     * @return true if found and updated, false otherwise
     */
    public boolean setObbligatorietaCampoSpecifico(String nomeCategoria, String nomeCampo, boolean obbligatorio)
    {
        Categoria cat = getCategoriaOrThrow(nomeCategoria);
        boolean updated = cat.setObbligatorietaCampoSpecifico(normalizza(nomeCampo), obbligatorio);
        if (updated) repo.save(catalogo);
        return updated;
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private boolean categoriaEsistente(String nome)
    {
        return catalogo.getCategorie().stream()
                .anyMatch(c -> c.getNome().equalsIgnoreCase(nome));
    }

    private String normalizza(String s)
    {
        if (s == null) throw new IllegalArgumentException("Nome non valido (null).");
        return s.trim();
    }

    public Catalogo getCatalogo() {
        return catalogo;
    }
}
