package it.unibs.ingsoft.v2.application;

import it.unibs.ingsoft.v2.domain.*;
import it.unibs.ingsoft.v2.persistence.api.ICategoriaRepository;
import it.unibs.ingsoft.v2.persistence.dto.CatalogoData;

import java.util.*;

/**
 * Manages base and common fields.
 *
 * <p>On construction, ensures the eight fixed base fields defined by
 * {@link CampoBaseDefinito} are always present (idempotent).</p>
 *
 * <p>SRP split: this class owns system-wide fields; {@link CategoriaService}
 * owns category definitions and category-specific fields.</p>
 */
public final class CampoService
{
    private final ICategoriaRepository repo;
    private final CatalogoData         catalogo;

    public CampoService(ICategoriaRepository repo)
    {
        this.repo     = Objects.requireNonNull(repo);
        this.catalogo = repo.load();
        inizializzaCampiBaseFissi();
    }

    // ---------------------------------------------------------------
    // CAMPI BASE
    // ---------------------------------------------------------------

    /** Ensures the eight predefined base fields are present. Idempotent. */
    private void inizializzaCampiBaseFissi()
    {
        boolean modificato = false;
        for (CampoBaseDefinito cbd : CampoBaseDefinito.values())
        {
            boolean giaPresente = catalogo.getCampiBase().stream()
                    .anyMatch(c -> c.getNome().equalsIgnoreCase(cbd.getNomeCampo()));
            if (!giaPresente)
            {
                catalogo.addCampoBase(new Campo(cbd.getNomeCampo(), TipoCampo.BASE, cbd.getTipoDato(), true));
                modificato = true;
            }
        }
        if (modificato) repo.save(catalogo);
    }

    /**
     * Adds optional extra base fields chosen at first startup.
     *
     * @pre !isCampiBaseFissati()
     * @post isCampiBaseFissati()
     * @throws IllegalStateException    if base fields are already fixed
     * @throws IllegalArgumentException if any name conflicts with an existing field
     */
    public void aggiungiCampiBaseExtra(List<String> nomi, List<TipoDato> tipi)
    {
        if (catalogo.isCampiBaseFissati())
            throw new IllegalStateException("I campi base extra sono già stati fissati e sono immutabili.");
        Objects.requireNonNull(nomi);
        Objects.requireNonNull(tipi);

        for (int i = 0; i < nomi.size(); i++)
        {
            String nome = normalizza(nomi.get(i));
            TipoDato td = tipi.get(i);

            if (CampoBaseDefinito.isNomeFisso(nome))
                throw new IllegalArgumentException("\"" + nome + "\" è un campo base fisso; non può essere aggiunto come extra.");
            if (nomeEsiste(nome))
                throw new IllegalArgumentException("Esiste già un campo con il nome: \"" + nome + "\".");

            catalogo.addCampoBase(new Campo(nome, TipoCampo.BASE, td, true));
        }
        catalogo.setCampiBaseFissati();
        repo.save(catalogo);
    }

    /** Marks base fields as fixed without adding extras. */
    public void fissaCampiBaseSenzaExtra()
    {
        if (!catalogo.isCampiBaseFissati())
        {
            catalogo.setCampiBaseFissati();
            repo.save(catalogo);
        }
    }

    public boolean isCampiBaseFissati()  { return catalogo.isCampiBaseFissati(); }
    public List<Campo> getCampiBase()    { return catalogo.getCampiBase(); }

    // ---------------------------------------------------------------
    // CAMPI COMUNI
    // ---------------------------------------------------------------

    /**
     * Adds a new common field shared by all categories.
     *
     * @throws IllegalArgumentException if a field with the same name already exists globally
     */
    public void addCampoComune(String nome, TipoDato tipoDato, boolean obbligatorio)
    {
        nome = normalizza(nome);
        if (nomeEsiste(nome))
            throw new IllegalArgumentException("Esiste già un campo con il nome: \"" + nome + "\".");
        catalogo.addCampoComune(new Campo(nome, TipoCampo.COMUNE, tipoDato, obbligatorio));
        repo.save(catalogo);
    }

    /**
     * Removes the common field with the given name (case-insensitive).
     *
     * @return true if removed, false if not found
     */
    public boolean removeCampoComune(String nome)
    {
        boolean removed = catalogo.removeCampoComune(normalizza(nome));
        if (removed) repo.save(catalogo);
        return removed;
    }

    /**
     * Changes the mandatory flag of an existing common field.
     * Replaces the existing (immutable) {@link Campo} with a new instance via
     * {@link Campo#withObbligatorio(boolean)}.
     *
     * @return true if found and updated, false otherwise
     */
    public boolean setObbligatorietaCampoComune(String nome, boolean obbligatorio)
    {
        nome = normalizza(nome);
        for (Campo c : catalogo.getCampiComuni())
        {
            if (c.getNome().equalsIgnoreCase(nome))
            {
                catalogo.replaceCampoComune(nome, c.withObbligatorio(obbligatorio));
                repo.save(catalogo);
                return true;
            }
        }
        return false;
    }

    public List<Campo> getCampiComuni() { return catalogo.getCampiComuni(); }

    // ---------------------------------------------------------------
    // Uniqueness helpers (package-visible for CategoriaService)
    // ---------------------------------------------------------------

    /** True if a base or common field with this name already exists. */
    boolean nomeBaseOComuneEsistente(String nome)
    {
        for (Campo c : catalogo.getCampiBase())
            if (c.getNome().equalsIgnoreCase(nome)) return true;
        for (Campo c : catalogo.getCampiComuni())
            if (c.getNome().equalsIgnoreCase(nome)) return true;
        return false;
    }

    /** Full global uniqueness check: base + common + all specific fields across all categories. */
    public boolean nomeEsiste(String nome)
    {
        if (nomeBaseOComuneEsistente(nome)) return true;
        for (Categoria cat : catalogo.getCategorie())
            for (Campo c : cat.getCampiSpecifici())
                if (c.getNome().equalsIgnoreCase(nome)) return true;
        return false;
    }

    private String normalizza(String s)
    {
        if (s == null) throw new IllegalArgumentException("Nome non valido (null).");
        return s.trim();
    }
}
