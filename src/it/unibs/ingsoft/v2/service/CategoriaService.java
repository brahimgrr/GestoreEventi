package it.unibs.ingsoft.v2.service;

import it.unibs.ingsoft.v2.model.*;
import it.unibs.ingsoft.v2.persistence.AppData;
import it.unibs.ingsoft.v2.persistence.IPersistenceService;

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
        this.db   = Objects.requireNonNull(db);
        this.data = Objects.requireNonNull(data);

        // Assicura che i campi base fissi siano sempre presenti
        inizializzaCampiBaseFissi();
    }

    // ---------------------------------------------------------------
    // CAMPI BASE
    // ---------------------------------------------------------------

    /**
     * Inserisce i campi base fissi della traccia se non sono ancora presenti.
     * Viene chiamato ad ogni avvio: è idempotente.
     */
    private void inizializzaCampiBaseFissi()
    {
        boolean modificato = false;

        for (CampoBaseDefinito cbd : CampoBaseDefinito.values())
        {
            boolean giaPresente = data.getCampiBase().stream()
                    .anyMatch(c -> c.getNome().equalsIgnoreCase(cbd.getNomeCampo()));

            if (!giaPresente)
            {
                data.addCampoBase(new Campo(cbd.getNomeCampo(), TipoCampo.BASE, cbd.getTipoDato(), true));
                modificato = true;
            }
        }

        if (modificato)
            db.save(data);
    }

    /**
     * Adds extra base fields chosen by the configurator at first startup.
     * The fixed base fields are already present; this method adds optional extras only.
     *
     * @pre  !data.isCampiBaseFissati()
     * @pre  nomi != null &amp;&amp; tipi != null &amp;&amp; nomi.size() == tipi.size()
     * @post data.isCampiBaseFissati()
     * @throws IllegalStateException    if base fields are already fixed
     * @throws IllegalArgumentException if any name conflicts with an existing field
     *
     * Aggiunge campi base EXTRA scelti dal configuratore al primo avvio.
     * I campi fissi della traccia sono già presenti; questo metodo aggiunge
     * solo quelli aggiuntivi. Una volta salvati, non potranno essere modificati.
     *
     *
     */
    public void aggiungiCampiBaseExtra(List<String> nomi, List<TipoDato> tipi)
    {
        if (data.isCampiBaseFissati())
            throw new IllegalStateException("I campi base extra sono già stati fissati e sono immutabili.");

        Objects.requireNonNull(nomi);
        Objects.requireNonNull(tipi);

        for (int i = 0; i < nomi.size(); i++)
        {
            String nome = normalizza(nomi.get(i));
            TipoDato td = tipi.get(i);

            if (CampoBaseDefinito.isNomeFisso(nome))
                throw new IllegalArgumentException("\"" + nome + "\" è un campo base fisso; non può essere aggiunto come extra.");

            if (nomeCampoGiaEsistente(nome))
                throw new IllegalArgumentException("Esiste già un campo con il nome: \"" + nome + "\".");

            data.addCampoBase(new Campo(nome, TipoCampo.BASE, td, true));
        }

        data.setCampiBaseFissati(true);
        db.save(data);
    }

    /**
     * Marca i campi base come fissati senza aggiungere extra.
     * Da chiamare se il configuratore non vuole aggiungere campi base extra.
     */
    public void fissaCampiBaseSenzaExtra()
    {
        if (!data.isCampiBaseFissati())
        {
            data.setCampiBaseFissati(true);
            db.save(data);
        }
    }

    public boolean isCampiBaseFissati()
    {
        return data.isCampiBaseFissati();
    }

    public List<Campo> getCampiBase()
    {
        return Collections.unmodifiableList(data.getCampiBase());
    }

    // ---------------------------------------------------------------
    // CAMPI COMUNI
    // ---------------------------------------------------------------

    /**
     * Adds a new common field shared by all categories.
     *
     * @pre  nome != null &amp;&amp; !nome.isBlank()
     * @pre  tipoDato != null
     * @pre  no existing field (base, common, or specific) has the same name (case-insensitive)
     * @post getCampiComuni() contains a field with the given name
     * @throws IllegalArgumentException if a field with the same name already exists
     */
    public void addCampoComune(String nome, TipoDato tipoDato, boolean obbligatorio)
    {
        nome = normalizza(nome);

        if (nomeCampoGiaEsistente(nome))
            throw new IllegalArgumentException("Esiste già un campo con il nome: \"" + nome + "\".");

        data.addCampoComune(new Campo(nome, TipoCampo.COMUNE, tipoDato, obbligatorio));
        db.save(data);
    }

    public boolean removeCampoComune(String nome)
    {
        final String n = normalizza(nome);
        boolean removed = data.removeCampoComune(n);
        if (removed) db.save(data);
        return removed;
    }

    public boolean setObbligatorietaCampoComune(String nome, boolean obbligatorio)
    {
        nome = normalizza(nome);

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

    // ---------------------------------------------------------------
    // CATEGORIE
    // ---------------------------------------------------------------

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
        nomeCategoria = normalizza(nomeCategoria);

        if (data.findCategoria(nomeCategoria).isPresent())
            throw new IllegalArgumentException("Categoria già esistente.");

        Categoria cat = new Categoria(nomeCategoria);
        data.addCategoria(cat);
        db.save(data);
        return cat;
    }

    public boolean removeCategoria(String nomeCategoria)
    {
        final String n = normalizza(nomeCategoria);
        boolean removed = data.removeCategoria(n);

        if (removed)
            db.save(data);

        return removed;
    }

    public List<Categoria> getCategorie()
    {
        return Collections.unmodifiableList(data.getCategorie());
    }

    public Categoria getCategoriaOrThrow(String nomeCategoria)
    {
        final String nome = normalizza(nomeCategoria);
        return data.findCategoria(nome)
                   .orElseThrow(() -> new IllegalArgumentException("Categoria non trovata: \"" + nome + "\"."));
    }

    // ---------------------------------------------------------------
    // CAMPI SPECIFICI
    // ---------------------------------------------------------------

    /**
     * Adds a specific field to the given category.
     *
     * @pre  nomeCategoria != null &amp;&amp; the category exists
     * @pre  nomeCampo != null &amp;&amp; !nomeCampo.isBlank()
     * @pre  tipoDato != null
     * @pre  no base or common field has the same name (case-insensitive)
     * @pre  the category does not already have a specific field with the same name
     * @post the category's specific fields contain a field named {@code nomeCampo}
     * @throws IllegalArgumentException if category not found, or name conflicts with existing field
     */
    public void addCampoSpecifico(String nomeCategoria, String nomeCampo, TipoDato tipoDato, boolean obbligatorio)
    {
        nomeCampo = normalizza(nomeCampo);

        // Global check: base and common fields must not be shadowed
        if (nomeCampoBaseOComuneGiaEsistente(nomeCampo))
            throw new IllegalArgumentException("Esiste già un campo con il nome: \"" + nomeCampo + "\".");

        Categoria c = getCategoriaOrThrow(nomeCategoria);

        // Per-category check: unique within this category's specific fields
        final String nome = nomeCampo;
        if (c.getCampiSpecifici().stream().anyMatch(f -> f.getNome().equalsIgnoreCase(nome)))
            throw new IllegalArgumentException("Esiste già un campo specifico con il nome: \"" + nome + "\" in questa categoria.");

        c.addCampoSpecifico(new Campo(nomeCampo, TipoCampo.SPECIFICO, tipoDato, obbligatorio));
        db.save(data);
    }

    public boolean removeCampoSpecifico(String nomeCategoria, String nomeCampo)
    {
        nomeCampo = normalizza(nomeCampo);
        Categoria c = getCategoriaOrThrow(nomeCategoria);
        boolean removed = c.removeCampoSpecifico(nomeCampo);

        if (removed)
            db.save(data);

        return removed;
    }

    public boolean setObbligatorietaCampoSpecifico(String nomeCategoria, String nomeCampo, boolean obbligatorio)
    {
        nomeCampo = normalizza(nomeCampo);
        Categoria c = getCategoriaOrThrow(nomeCategoria);
        boolean ok = c.setObbligatorietaCampoSpecifico(nomeCampo, obbligatorio);

        if (ok)
            db.save(data);
        return ok;
    }

    // ---------------------------------------------------------------
    // UTILITY
    // ---------------------------------------------------------------

    private boolean nomeCampoGiaEsistente(String nome)
    {
        nome = normalizza(nome);
        for (Campo c : getTuttiICampi())
            if (c.getNome().equalsIgnoreCase(nome))
                return true;
        return false;
    }

    private List<Campo> getTuttiICampi()
    {
        List<Campo> all = new ArrayList<>();
        all.addAll(data.getCampiBase());
        all.addAll(data.getCampiComuni());

        for (Categoria c : data.getCategorie())
            all.addAll(c.getCampiSpecifici());

        return all;
    }

    // Checks only base and common fields (used for specific-field uniqueness within a category)
    private boolean nomeCampoBaseOComuneGiaEsistente(String nome)
    {
        nome = normalizza(nome);
        for (Campo c : data.getCampiBase())
            if (c.getNome().equalsIgnoreCase(nome)) return true;
        for (Campo c : data.getCampiComuni())
            if (c.getNome().equalsIgnoreCase(nome)) return true;
        return false;
    }

    private String normalizza(String s)
    {
        if (s == null) throw new IllegalArgumentException("Nome non valido (null).");
        return s.trim();
    }


    // ---------------------------------------------------------------
    // RECORD DI SUPPORTO (NON UTILIZZO)
    // ---------------------------------------------------------------

    /**
     * Coppia (nome campo, tipo dato) usata per la creazione di campi extra/comuni/specifici.
     *
     public record NomeTipo(String nome, TipoDato tipoDato)
     {
     }
     */
}