package it.unibs.ingsoft.v5.service;

import it.unibs.ingsoft.v5.model.*;
import it.unibs.ingsoft.v5.persistence.AppData;
import it.unibs.ingsoft.v5.persistence.IPersistenceService;

import java.util.*;

public final class CategoriaService
{
    private final IPersistenceService db;
    private final AppData data;

    /**
     * @pre db != null
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
     * Aggiunge campi base EXTRA scelti dal configuratore al primo avvio.
     * I campi fissi della traccia sono già presenti; questo metodo aggiunge
     * solo quelli aggiuntivi. Una volta salvati, non potranno essere modificati.
     *
     *
     */
    /**
     * Aggiunge campi base EXTRA scelti dal configuratore al primo avvio.
     *
     * @pre  nomi != null
     * @pre  tipi != null
     * @pre  !isCampiBaseFissati()
     * @post isCampiBaseFissati() == true
     * @post data.getCampiBase() contains all previously present fields plus the new ones
     * @throws IllegalStateException    if campi base are already fixed
     * @throws IllegalArgumentException if any name is a reserved fixed field or already exists
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
    /**
     * Marca i campi base come fissati senza aggiungere extra.
     *
     * @post isCampiBaseFissati() == true
     */
    public void fissaCampiBaseSenzaExtra()
    {
        if (!data.isCampiBaseFissati())
        {
            data.setCampiBaseFissati(true);
            db.save(data);
        }
    }

    /**
     * @post result == data.isCampiBaseFissati()
     */
    public boolean isCampiBaseFissati()
    {
        return data.isCampiBaseFissati();
    }

    /**
     * @post result is an unmodifiable view of the base fields list
     */
    public List<Campo> getCampiBase()
    {
        return Collections.unmodifiableList(data.getCampiBase());
    }

    // ---------------------------------------------------------------
    // CAMPI COMUNI
    // ---------------------------------------------------------------

    /**
     * @pre  nome != null
     * @pre  tipoDato != null
     * @post getCampiComuni() contains a new Campo with the given name
     * @throws IllegalArgumentException if a field with that name already exists
     */
    public void addCampoComune(String nome, TipoDato tipoDato, boolean obbligatorio)
    {
        nome = normalizza(nome);

        if (nomeCampoGiaEsistente(nome))
            throw new IllegalArgumentException("Esiste già un campo con il nome: \"" + nome + "\".");

        data.addCampoComune(new Campo(nome, TipoCampo.COMUNE, tipoDato, obbligatorio));
        db.save(data);
    }

    /**
     * @pre  nome != null
     * @post result == true implies getCampiComuni() no longer contains a field named nome
     */
    public boolean removeCampoComune(String nome)
    {
        final String n = normalizza(nome);
        boolean removed = data.removeCampoComune(n);
        if (removed) db.save(data);
        return removed;
    }

    /**
     * @pre  nome != null
     * @post result == true implies the named common field has obbligatorio == obbligatorio
     */
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

    /**
     * @post result is an unmodifiable view of the common fields list
     */
    public List<Campo> getCampiComuni()
    {
        return Collections.unmodifiableList(data.getCampiComuni());
    }

    // ---------------------------------------------------------------
    // CATEGORIE
    // ---------------------------------------------------------------

    /**
     * @pre  nomeCategoria != null
     * @post getCategorie() contains a new Categoria with the given name
     * @throws IllegalArgumentException if a categoria with that name already exists
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

    /**
     * @pre  nomeCategoria != null
     * @post result == true implies getCategorie() no longer contains a categoria named nomeCategoria
     */
    public boolean removeCategoria(String nomeCategoria)
    {
        final String n = normalizza(nomeCategoria);
        boolean removed = data.removeCategoria(n);

        if (removed)
            db.save(data);

        return removed;
    }

    /**
     * @post result is an unmodifiable view of the categoria list
     */
    public List<Categoria> getCategorie()
    {
        return Collections.unmodifiableList(data.getCategorie());
    }

    /**
     * @pre  nomeCategoria != null
     * @post result != null
     * @throws IllegalArgumentException if no categoria with that name exists
     */
    /**
     * @pre  nomeCategoria != null
     * @post result != null
     * @throws IllegalArgumentException if no categoria with that name exists
     */
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
     * @pre  nomeCategoria != null
     * @pre  nomeCampo != null
     * @pre  tipoDato != null
     * @post getCategoria(nomeCategoria).getCampiSpecifici() contains a new Campo named nomeCampo
     * @throws IllegalArgumentException if the categoria does not exist, or a field with that name
     *                                  already exists as a base, common, or specific field in this categoria
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

    /**
     * @pre  nomeCategoria != null
     * @pre  nomeCampo != null
     * @post result == true implies the categoria no longer has a specific field named nomeCampo
     * @throws IllegalArgumentException if the categoria does not exist
     */
    public boolean removeCampoSpecifico(String nomeCategoria, String nomeCampo)
    {
        nomeCampo = normalizza(nomeCampo);
        Categoria c = getCategoriaOrThrow(nomeCategoria);
        boolean removed = c.removeCampoSpecifico(nomeCampo);

        if (removed)
            db.save(data);

        return removed;
    }

    /**
     * @pre  nomeCategoria != null
     * @pre  nomeCampo != null
     * @post result == true implies the named specific field has obbligatorio == obbligatorio
     * @throws IllegalArgumentException if the categoria does not exist
     */
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
    // BATCH IMPORT SUPPORT (package-private, no db.save)
    // ---------------------------------------------------------------

    void addCampoComuneNoSave(String nome, TipoDato tipoDato, boolean obbligatorio)
    {
        nome = normalizza(nome);

        if (nomeCampoGiaEsistente(nome))
            throw new IllegalArgumentException("Esiste già un campo con il nome: \"" + nome + "\".");

        data.addCampoComune(new Campo(nome, TipoCampo.COMUNE, tipoDato, obbligatorio));
    }

    Categoria createCategoriaNoSave(String nomeCategoria)
    {
        nomeCategoria = normalizza(nomeCategoria);

        if (data.findCategoria(nomeCategoria).isPresent())
            throw new IllegalArgumentException("Categoria già esistente.");

        Categoria cat = new Categoria(nomeCategoria);
        data.addCategoria(cat);
        return cat;
    }

    void addCampoSpecificoNoSave(String nomeCategoria, String nomeCampo,
                                  TipoDato tipoDato, boolean obbligatorio)
    {
        nomeCampo = normalizza(nomeCampo);

        if (nomeCampoBaseOComuneGiaEsistente(nomeCampo))
            throw new IllegalArgumentException("Esiste già un campo con il nome: \"" + nomeCampo + "\".");

        Categoria c = getCategoriaOrThrow(nomeCategoria);

        final String nome = nomeCampo;
        if (c.getCampiSpecifici().stream().anyMatch(f -> f.getNome().equalsIgnoreCase(nome)))
            throw new IllegalArgumentException("Esiste già un campo specifico con il nome: \"" + nome + "\" in questa categoria.");

        c.addCampoSpecifico(new Campo(nomeCampo, TipoCampo.SPECIFICO, tipoDato, obbligatorio));
    }

    void save()
    {
        db.save(data);
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