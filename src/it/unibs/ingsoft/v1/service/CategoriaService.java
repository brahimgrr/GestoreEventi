package it.unibs.ingsoft.v1.service;

import it.unibs.ingsoft.v1.model.Campo;
import it.unibs.ingsoft.v1.model.Categoria;
import it.unibs.ingsoft.v1.model.TipoCampo;
import it.unibs.ingsoft.v1.persistence.AppData;
import it.unibs.ingsoft.v1.persistence.IPersistenceService;

import java.util.*;

public final class CategoriaService
{
    private final IPersistenceService db;
    private final AppData data;

    /**
     * @pre db   != null
     * @pre data != null
     */
    public CategoriaService(IPersistenceService db, AppData data)
    {
        this.db = Objects.requireNonNull(db);
        this.data = Objects.requireNonNull(data);
    }

    // ---------- Campi base ----------

    /**
     * Fixes the base fields for the first and only time.
     *
     * @pre  !data.isCampiBaseFissati()
     * @pre  nomiCampiBase != null &amp;&amp; !nomiCampiBase.isEmpty()
     * @post data.isCampiBaseFissati()
     * @post getCampiBase().size() == (number of non-blank entries in nomiCampiBase)
     * @throws IllegalStateException    if base fields have already been fixed
     * @throws IllegalArgumentException if the list is empty or contains duplicates
     */
    public void fissareCampiBase(List<String> nomiCampiBase)
    {
        if (data.isCampiBaseFissati())
            throw new IllegalStateException("I campi base sono già stati fissati e sono immutabili.");

        Objects.requireNonNull(nomiCampiBase);

        List<String> clean = new ArrayList<>();

        for (String s : nomiCampiBase)
        {
            if (s == null || s.isBlank())
                continue;

            clean.add(s.trim());
        }

        if (clean.isEmpty())
            throw new IllegalArgumentException("Lista campi base vuota.");

        // Nomi univoci => uso Set (case-insensitive)
        Set<String> set = new HashSet<>();

        for (String n : clean)
        {
            String key = n.toLowerCase();

            if (!set.add(key))
                throw new IllegalArgumentException("Nome campo base duplicato: " + n);

            if (nomeCampoGiaEsistente(n))
                throw new IllegalArgumentException("Esiste già un campo con questo nome: " + n);
        }

        data.clearCampiBase();

        for (String n : clean)
            data.addCampoBase(new Campo(n, TipoCampo.BASE, true)); // tutti obbligatori

        data.setCampiBaseFissati(true);
        db.save(data);
    }

    public List<Campo> getCampiBase()
    {
        return Collections.unmodifiableList(data.getCampiBase());
    }

    // ---------- Campi comuni ----------

    /**
     * Adds a new common field shared by all categories.
     *
     * @pre  nome != null &amp;&amp; !nome.isBlank()
     * @pre  no existing field (base, common, or specific) has the same name (case-insensitive)
     * @post getCampiComuni() contains a field with the given name
     * @throws IllegalArgumentException if a field with the same name already exists
     */
    public void addCampoComune(String nome, boolean obbligatorio)
    {
        if (nomeCampoGiaEsistente(nome))
            throw new IllegalArgumentException("Esiste già un campo con questo nome.");

        data.addCampoComune(new Campo(nome, TipoCampo.COMUNE, obbligatorio));
        db.save(data);
    }

    /*
    private void assicuraCampoComuneUnico(String nome)
    {
        for (Campo c : data.getCampiComuni())
        {
            if (c.getNome().equalsIgnoreCase(nome))
                throw new IllegalArgumentException("Esiste già un campo comune con questo nome.");
        }
    }
    */

    /**
     * Removes the common field with the given name (case-insensitive).
     *
     * @pre  nome != null
     * @post getCampiComuni() no longer contains a field whose name equals {@code nome} (case-insensitive)
     * @return {@code true} if the field was found and removed, {@code false} otherwise
     */
    public boolean removeCampoComune(String nome)
    {
        boolean removed = data.removeCampoComune(nome);

        if (removed)
            db.save(data);

        return removed;
    }

    /**
     * Changes the mandatory flag of an existing common field.
     *
     * @pre  nome != null
     * @post if the field exists, its {@code obbligatorio} flag equals {@code obbligatorio}
     * @return {@code true} if the field was found and updated, {@code false} otherwise
     */
    public boolean setObbligatorietaCampoComune(String nome, boolean obbligatorio)
    {
        for (Campo c : data.getCampiComuni())
        {
            if (c.getNome().equalsIgnoreCase(nome))
            {
                c.setObbligatorio(obbligatorio);
                db.save(data);
                return true;
            }
        }

        return false;
    }

    public List<Campo> getCampiComuni()
    {
        return Collections.unmodifiableList(data.getCampiComuni());
    }


    // ---------- Categorie ----------

    /**
     * Creates a new category with the given name.
     *
     * @pre  nomeCategoria != null &amp;&amp; !nomeCategoria.isBlank()
     * @pre  no category with this name already exists (case-insensitive)
     * @post getCategorie() contains a category named {@code nomeCategoria}
     * @throws IllegalArgumentException if a category with the same name already exists
     */
    public Categoria createCategoria(String nomeCategoria)
    {
        if (data.findCategoria(nomeCategoria).isPresent())
            throw new IllegalArgumentException("Categoria già esistente.");

        Categoria cat = new Categoria(nomeCategoria);
        data.addCategoria(cat);
        db.save(data);
        return cat;
    }

    /**
     * Removes the category with the given name (case-insensitive), including all its specific fields.
     *
     * @pre  nomeCategoria != null
     * @post getCategorie() no longer contains a category named {@code nomeCategoria}
     * @return {@code true} if the category was found and removed, {@code false} otherwise
     */
    public boolean removeCategoria(String nomeCategoria)
    {
        boolean removed = data.removeCategoria(nomeCategoria);

        if (removed)
        {
            db.save(data);
        }

        return removed;
    }

    public List<Categoria> getCategorie()
    {
        return Collections.unmodifiableList(data.getCategorie());
    }

    public Categoria getCategoriaOrThrow(String nomeCategoria)
    {
        return data.findCategoria(nomeCategoria)
                   .orElseThrow(() -> new IllegalArgumentException("Categoria non trovata."));
    }

    /**
     * Adds a specific field to the given category.
     *
     * @pre  nomeCategoria != null &amp;&amp; the category exists
     * @pre  nomeCampo != null &amp;&amp; !nomeCampo.isBlank()
     * @pre  no base or common field has the same name (case-insensitive)
     * @pre  the category does not already have a specific field with the same name
     * @post the category's specific fields contain a field named {@code nomeCampo}
     * @throws IllegalArgumentException if category not found, or name conflicts with existing field
     */
    public void addCampoSpecifico(String nomeCategoria, String nomeCampo, boolean obbligatorio)
    {
        // Global check: base and common fields must not be shadowed
        if (nomeCampoBaseOComuneGiaEsistente(nomeCampo))
            throw new IllegalArgumentException("Esiste già un campo con questo nome.");

        Categoria c = getCategoriaOrThrow(nomeCategoria);

        // Per-category check: unique within this category's specific fields
        if (c.getCampiSpecifici().stream().anyMatch(f -> f.getNome().equalsIgnoreCase(nomeCampo)))
            throw new IllegalArgumentException("Esiste già un campo specifico con questo nome in questa categoria.");

        c.addCampoSpecifico(new Campo(nomeCampo, TipoCampo.SPECIFICO, obbligatorio));
        db.save(data);
    }

    /**
     * Removes a specific field from the given category.
     *
     * @pre  nomeCategoria != null
     * @pre  nomeCampo != null
     * @post the category no longer contains a specific field named {@code nomeCampo}
     * @return {@code true} if the field was found and removed, {@code false} otherwise
     * @throws IllegalArgumentException if the category is not found
     */
    public boolean removeCampoSpecifico(String nomeCategoria, String nomeCampo)
    {
        Categoria c = getCategoriaOrThrow(nomeCategoria);
        boolean removed = c.removeCampoSpecifico(nomeCampo);

        if (removed)
            db.save(data);

        return removed;
    }

    /**
     * Changes the mandatory flag of a specific field in the given category.
     *
     * @pre  nomeCategoria != null
     * @pre  nomeCampo != null
     * @post if the field exists, its {@code obbligatorio} flag equals {@code obbligatorio}
     * @return {@code true} if the field was found and updated, {@code false} otherwise
     * @throws IllegalArgumentException if the category is not found
     */
    public boolean setObbligatorietaCampoSpecifico(String nomeCategoria, String nomeCampo, boolean obbligatorio)
    {
        Categoria c = getCategoriaOrThrow(nomeCategoria);
        boolean ok = c.setObbligatorietaCampoSpecifico(nomeCampo, obbligatorio);

        if (ok)
            db.save(data);

        return ok;
    }

    private boolean nomeCampoGiaEsistente(String nome)
    {
        String key = nome.toLowerCase();

        // controlla campi base
        for (Campo c : data.getCampiBase())
            if (c.getNome().equalsIgnoreCase(key))
                return true;

        // controlla campi comuni
        for (Campo c : data.getCampiComuni())
            if (c.getNome().equalsIgnoreCase(key))
                return true;

        // controlla campi specifici di tutte le categorie
        for (Categoria cat : data.getCategorie())
            for (Campo c : cat.getCampiSpecifici())
                if (c.getNome().equalsIgnoreCase(key))
                    return true;

        return false;
    }

    // Checks only base and common fields (used for specific-field uniqueness within a category)
    private boolean nomeCampoBaseOComuneGiaEsistente(String nome)
    {
        String key = nome.toLowerCase();

        for (Campo c : data.getCampiBase())
            if (c.getNome().equalsIgnoreCase(key))
                return true;

        for (Campo c : data.getCampiComuni())
            if (c.getNome().equalsIgnoreCase(key))
                return true;

        return false;
    }

    /*
    // ---------- Visualizzazione unificata ----------
    public String toStringCategoriaService(String nomeCategoria)
    {
        Categoria cat = getCategoriaOrThrow(nomeCategoria);

        StringBuilder sb = new StringBuilder();
        sb.append("it.unibs.ingsoft.v1.model.Categoria: ").append(cat.getNome()).append("\n");

        sb.append("  Campi BASE (immutabili):\n");
        for (Campo c : data.getCampiBase())
            sb.append("   - ").append(c).append("\n");

        sb.append("  Campi COMUNI:\n");
        if (data.getCampiComuni().isEmpty())
            sb.append("   (nessuno)\n");

        for (Campo c : data.getCampiComuni())
            sb.append("   - ").append(c).append("\n");

        sb.append("  Campi SPECIFICI:\n");
        if (cat.getCampiSpecifici().isEmpty())
            sb.append("   (nessuno)\n");

        for (Campo c : cat.getCampiSpecifici())
            sb.append("   - ").append(c).append("\n");

        return sb.toString();
    }
    */
}