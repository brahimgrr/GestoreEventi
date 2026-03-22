package it.unibs.ingsoft.v1.application;

import it.unibs.ingsoft.v1.domain.Campo;
import it.unibs.ingsoft.v1.domain.CampoBaseDefinito;
import it.unibs.ingsoft.v1.domain.Categoria;
import it.unibs.ingsoft.v1.domain.TipoCampo;
import it.unibs.ingsoft.v1.domain.TipoDato;
import it.unibs.ingsoft.v1.domain.Catalogo;
import it.unibs.ingsoft.v1.persistence.api.ICatalogoRepository;

import java.util.*;

/** Manages base fields, common fields, categories and category-specific fields. */
public final class CatalogoService
{
    private final ICatalogoRepository repo;
    private final Catalogo catalogo;

    public CatalogoService(ICatalogoRepository repo)
    {
        this.repo     = Objects.requireNonNull(repo);
        this.catalogo = repo.load();
    }

    // ---------------------------------------------------------------
    // CAMPI BASE (one-time setup)
    // ---------------------------------------------------------------

    /** Loads the predefined base fields once and marks them as immutable. */
    public void fissareCampiBase()
    {
        if (catalogo.isCampiBaseFissati())
            throw new IllegalStateException("I campi base sono già stati fissati e sono immutabili.");

        catalogo.clearCampiBase();
        for (Campo c : CampoBaseDefinito.getAll())
            catalogo.addCampoBase(c);
        catalogo.setCampiBaseFissati();
        repo.save(catalogo);
    }

    /**
     * Loads predefined base fields plus any extra ones, then marks them as immutable.
     *
     * @throws IllegalArgumentException if an extra name is duplicated or already used
     */
    public void fissareCampiBaseConExtra(List<String> nomiExtra)
    {
        if (catalogo.isCampiBaseFissati())
            throw new IllegalStateException("I campi base sono già stati fissati e sono immutabili.");

        catalogo.clearCampiBase();
        for (Campo c : CampoBaseDefinito.getAll())
            catalogo.addCampoBase(c);

        if (nomiExtra != null)
        {
            Set<String> seen = new HashSet<>();
            for (Campo c : CampoBaseDefinito.getAll())
                seen.add(c.getNome().toLowerCase());

            for (String s : nomiExtra)
            {
                if (s == null || s.isBlank()) continue;
                String nome = s.trim();

                if (!seen.add(nome.toLowerCase()))
                    throw new IllegalArgumentException("Nome campo base duplicato: " + nome);

                if (nomeEsistente(nome))
                    throw new IllegalArgumentException("Esiste già un campo con questo nome: " + nome);

                catalogo.addCampoBase(new Campo(nome, TipoCampo.BASE, TipoDato.STRINGA, true));
            }
        }

        catalogo.setCampiBaseFissati();
        repo.save(catalogo);
    }

    public List<Campo> getCampiBase()
    {
        return catalogo.getCampiBase();
    }

    // ---------------------------------------------------------------
    // CAMPI COMUNI
    // ---------------------------------------------------------------

    /** Adds a common field if its name is valid and not already used. */
    public void addCampoComune(String nome, TipoDato tipoDato, boolean obbligatorio)
    {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Il nome del campo non può essere vuoto.");

        if (nomeEsistente(nome))
            throw new IllegalArgumentException("Esiste già un campo con questo nome.");

        catalogo.addCampoComune(new Campo(nome.trim(), TipoCampo.COMUNE, tipoDato, obbligatorio));
        repo.save(catalogo);
    }

    /** @return {@code true} if the field was found and removed */
    public boolean removeCampoComune(String nome)
    {
        return saveIf(catalogo.removeCampoComune(nome));
    }

    /** Updates the required flag of a common field. */
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

    /** Creates a category if its name is not already used. */
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
     * Adds a category-specific field if the category exists and the name is valid.
     *
     * @throws IllegalArgumentException if the category is missing or the name is already used
     */
    public void addCampoSpecifico(String nomeCategoria, String nomeCampo, TipoDato tipoDato,
                                  boolean obbligatorio)
    {
        if (nomeCampo == null || nomeCampo.isBlank())
            throw new IllegalArgumentException("Il nome del campo non può essere vuoto.");

        if (nomeEsistente(nomeCampo))
            throw new IllegalArgumentException("Esiste già un campo con questo nome.");

        Categoria c = getCategoriaOrThrow(nomeCategoria);
        // In-category duplicate check and type check delegated to the domain model
        c.addCampoSpecifico(new Campo(nomeCampo.trim(), TipoCampo.SPECIFICO, tipoDato, obbligatorio));
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

    /** Updates the required flag of a category-specific field. */
    public boolean setObbligatorietaCampoSpecifico(String nomeCategoria, String nomeCampo,
                                                    boolean obbligatorio)
    {
        Categoria c = getCategoriaOrThrow(nomeCategoria);
        boolean ok = c.setObbligatorietaCampoSpecifico(nomeCampo, obbligatorio);
        return saveIf(ok);
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private Optional<Categoria> findCategoria(String nome)
    {
        return catalogo.getCategorie().stream()
                .filter(c -> c.getNome().equalsIgnoreCase(nome))
                .findFirst();
    }

    /**
     * Checks whether a field name already exists among base or common fields.
     * Specific field uniqueness is per-category only, enforced by
     * {@link Categoria#addCampoSpecifico(Campo)}.
     *
     * <p>Per spec: "Relativamente a ogni singola categoria, ogni campo è dotato
     * di un nome che lo individua univocamente" — uniqueness is within the scope
     * of base+common (globally) and specific (per-category).</p>
     */
    public boolean nomeEsistente(String nome)
    {
        for (Campo c : catalogo.getCampiBase())
            if (c.getNome().equalsIgnoreCase(nome)) return true;
        for (Campo c : catalogo.getCampiComuni())
            if (c.getNome().equalsIgnoreCase(nome)) return true;
        return false;
    }

    /** Saves the catalogue only if a change actually occurred. */
    private boolean saveIf(boolean changed)
    {
        if (changed) repo.save(catalogo);
        return changed;
    }
}
