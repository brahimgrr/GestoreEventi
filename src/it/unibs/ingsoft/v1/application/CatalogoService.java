package it.unibs.ingsoft.v1.application;

import it.unibs.ingsoft.v1.domain.Campo;
import it.unibs.ingsoft.v1.domain.Categoria;
import it.unibs.ingsoft.v1.domain.TipoCampo;
import it.unibs.ingsoft.v1.persistence.dto.CatalogoData;
import it.unibs.ingsoft.v1.persistence.api.ICategoriaRepository;

import java.util.*;

/**
 * Single service owning the catalogue (base fields, common fields, categories, specific fields).
 * Replaces the former {@code CampoService} + {@code CategoriaService} split, which introduced
 * shared-mutable-state coupling and a package-private cross-service dependency.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Global field-name uniqueness (base + common + all specific fields)</li>
 *   <li>Base-field one-time setup</li>
 *   <li>Common-field CRUD</li>
 *   <li>Category CRUD</li>
 *   <li>Specific-field CRUD (cross-category uniqueness checked here; in-category
 *       uniqueness enforced by the {@link Categoria} domain object)</li>
 * </ul>
 */
public final class CatalogoService
{
    private final ICategoriaRepository repo;
    private final CatalogoData         catalogo;

    public CatalogoService(ICategoriaRepository repo, CatalogoData catalogo)
    {
        this.repo     = Objects.requireNonNull(repo);
        this.catalogo = Objects.requireNonNull(catalogo);
    }

    // ---------------------------------------------------------------
    // CAMPI BASE (one-time setup)
    // ---------------------------------------------------------------

    /**
     * Fixes the base fields for the first and only time.
     *
     * @throws IllegalStateException    if base fields have already been fixed
     * @throws IllegalArgumentException if the list is empty or contains duplicates/conflicts
     */
    public void fissareCampiBase(List<String> nomiCampiBase)
    {
        if (catalogo.isCampiBaseFissati())
            throw new IllegalStateException("I campi base sono già stati fissati e sono immutabili.");

        Objects.requireNonNull(nomiCampiBase);

        List<String> clean = new ArrayList<>();
        for (String s : nomiCampiBase)
        {
            if (s == null || s.isBlank()) continue;
            clean.add(s.trim());
        }

        if (clean.isEmpty())
            throw new IllegalArgumentException("Lista campi base vuota.");

        Set<String> seen = new HashSet<>();
        for (String n : clean)
        {
            if (!seen.add(n.toLowerCase()))
                throw new IllegalArgumentException("Nome campo base duplicato: " + n);

            if (nomeEsistente(n))
                throw new IllegalArgumentException("Esiste già un campo con questo nome: " + n);
        }

        catalogo.clearCampiBase();
        for (String n : clean)
            catalogo.addCampoBase(new Campo(n, TipoCampo.BASE, true));
        catalogo.markCampiBaseFissati();
        repo.save(catalogo);
    }

    public List<Campo> getCampiBase()
    {
        return catalogo.getCampiBase();
    }

    // ---------------------------------------------------------------
    // CAMPI COMUNI
    // ---------------------------------------------------------------

    /**
     * @throws IllegalArgumentException if nome is blank or a field with this name already exists
     */
    public void addCampoComune(String nome, boolean obbligatorio)
    {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Il nome del campo non può essere vuoto.");

        if (nomeEsistente(nome))
            throw new IllegalArgumentException("Esiste già un campo con questo nome.");

        catalogo.addCampoComune(new Campo(nome.trim(), TipoCampo.COMUNE, obbligatorio));
        repo.save(catalogo);
    }

    /** @return {@code true} if the field was found and removed */
    public boolean removeCampoComune(String nome)
    {
        return saveIf(catalogo.removeCampoComune(nome));
    }

    /**
     * Updates the {@code obbligatorio} flag of a common field.
     * Because {@link Campo} is immutable, the element is replaced in the list.
     *
     * @return {@code true} if the field was found and updated
     */
    public boolean setObbligatorietaCampoComune(String nome, boolean obbligatorio)
    {
        Campo vecchio = catalogo.getCampiComuni().stream()
                .filter(c -> c.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .orElse(null);

        if (vecchio == null) return false;

        return saveIf(catalogo.replaceCampoComune(nome, vecchio.withObbligatorio(obbligatorio)));
    }

    public List<Campo> getCampiComuni()
    {
        return catalogo.getCampiComuni();
    }

    /** Returns base fields followed by common fields — all fields shared by every category. */
    public List<Campo> getCampiCondivisi()
    {
        List<Campo> condivisi = new ArrayList<>(catalogo.getCampiBase());
        condivisi.addAll(catalogo.getCampiComuni());
        return condivisi;
    }

    // ---------------------------------------------------------------
    // CATEGORIE
    // ---------------------------------------------------------------

    /**
     * @throws IllegalArgumentException if a category with this name already exists
     */
    public Categoria createCategoria(String nomeCategoria)
    {
        if (findCategoria(nomeCategoria).isPresent())
            throw new IllegalArgumentException("Categoria già esistente.");

        Categoria cat = new Categoria(nomeCategoria);
        catalogo.addCategoria(cat);
        repo.save(catalogo);
        return cat;
    }

    /** @return {@code true} if the category was found and removed */
    public boolean removeCategoria(String nomeCategoria)
    {
        return saveIf(catalogo.removeCategoria(nomeCategoria));
    }

    public List<Categoria> getCategorie()
    {
        return catalogo.getCategorie();
    }

    public Categoria getCategoriaOrThrow(String nomeCategoria)
    {
        return findCategoria(nomeCategoria)
                .orElseThrow(() -> new IllegalArgumentException("Categoria non trovata."));
    }

    // ---------------------------------------------------------------
    // CAMPI SPECIFICI
    // ---------------------------------------------------------------

    /**
     * Adds a specific field to the given category.
     * Cross-category uniqueness is checked here; in-category uniqueness and type validation
     * are enforced by {@link Categoria#addCampoSpecifico(Campo)}.
     *
     * @throws IllegalArgumentException if the category is not found, the name is blank,
     *                                  or a field with this name already exists globally
     */
    public void addCampoSpecifico(String nomeCategoria, String nomeCampo, boolean obbligatorio)
    {
        if (nomeCampo == null || nomeCampo.isBlank())
            throw new IllegalArgumentException("Il nome del campo non può essere vuoto.");

        if (nomeEsistente(nomeCampo))
            throw new IllegalArgumentException("Esiste già un campo con questo nome.");

        Categoria c = getCategoriaOrThrow(nomeCategoria);
        // In-category duplicate check and type check delegated to the domain model
        c.addCampoSpecifico(new Campo(nomeCampo.trim(), TipoCampo.SPECIFICO, obbligatorio));
        repo.save(catalogo);
    }

    /**
     * @return {@code true} if the field was found and removed
     * @throws IllegalArgumentException if the category is not found
     */
    public boolean removeCampoSpecifico(String nomeCategoria, String nomeCampo)
    {
        Categoria c = getCategoriaOrThrow(nomeCategoria);
        return saveIf(c.removeCampoSpecifico(nomeCampo));
    }

    /**
     * Updates the {@code obbligatorio} flag of a specific field.
     *
     * @return {@code true} if the field was found and updated
     * @throws IllegalArgumentException if the category is not found
     */
    public boolean setObbligatorietaCampoSpecifico(String nomeCategoria, String nomeCampo,
                                                    boolean obbligatorio)
    {
        Categoria c = getCategoriaOrThrow(nomeCategoria);
        boolean ok = c.setObbligatorietaCampoSpecifico(nomeCampo, obbligatorio);
        return saveIf(ok);
    }

    // ---------------------------------------------------------------
    // Read-only queries (UX guard — controllers use these for inline validation)
    // ---------------------------------------------------------------

    /** Returns {@code true} if any field (base, common, or specific) has the given name (case-insensitive). */
    public boolean nomeEsiste(String nome) { return nomeEsistente(nome); }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    private Optional<Categoria> findCategoria(String nome)
    {
        return catalogo.getCategorie().stream()
                .filter(c -> c.getNome().equalsIgnoreCase(nome))
                .findFirst();
    }

    /**
     * Global name uniqueness check: base fields + common fields + all specific fields
     * across all categories.
     */
    private boolean nomeEsistente(String nome)
    {
        if (Campo.containsNome(catalogo.getCampiBase(),   nome)) return true;
        if (Campo.containsNome(catalogo.getCampiComuni(), nome)) return true;

        for (Categoria cat : catalogo.getCategorie())
            if (Campo.containsNome(cat.getCampiSpecifici(), nome)) return true;

        return false;
    }

    /** Saves the catalogue only if a change actually occurred. */
    private boolean saveIf(boolean changed)
    {
        if (changed) repo.save(catalogo);
        return changed;
    }
}
