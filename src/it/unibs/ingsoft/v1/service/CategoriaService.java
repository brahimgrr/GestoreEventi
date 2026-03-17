package it.unibs.ingsoft.v1.service;

import it.unibs.ingsoft.v1.model.Campo;
import it.unibs.ingsoft.v1.model.Categoria;
import it.unibs.ingsoft.v1.model.TipoCampo;
import it.unibs.ingsoft.v1.persistence.AppData;
import it.unibs.ingsoft.v1.persistence.DatabaseService;

import java.util.*;

public final class CategoriaService
{
    private final DatabaseService db;
    private final AppData         data;

    public CategoriaService(DatabaseService db, AppData data)
    {
        this.db   = Objects.requireNonNull(db);
        this.data = Objects.requireNonNull(data);
    }

    // ---------------------------------------------------------------
    // CAMPI BASE
    // ---------------------------------------------------------------

    public void fissareCampiBase(List<String> nomiCampiBase)
    {
        if (data.isCampiBaseFissati())
            throw new IllegalStateException("I campi base sono già stati fissati e sono immutabili.");

        Objects.requireNonNull(nomiCampiBase);

        List<String> puliti = new ArrayList<>();

        for (String s : nomiCampiBase)
        {
            if (s == null || s.isBlank())
                continue;

            puliti.add(s.trim());
        }

        if (puliti.isEmpty())
            throw new IllegalArgumentException("Lista campi base vuota.");

        Set<String> set = new HashSet<>();

        for (String n : puliti)
        {
            String key = n.toLowerCase();

            if (!set.add(key))
                throw new IllegalArgumentException("Nome campo base duplicato: " + n);

            if (nomeCampoGiaEsistente(n))
                throw new IllegalArgumentException("Esiste già un campo con questo nome: " + n);
        }

        data.getCampiBase().clear();

        for (String n : puliti)
            data.getCampiBase().add(new Campo(n, TipoCampo.BASE, true));

        data.setCampiBaseFissati(true);
        db.save(data);
    }

    public List<Campo> getCampiBase()
    {
        return Collections.unmodifiableList(data.getCampiBase());
    }

    // ---------------------------------------------------------------
    // CAMPI COMUNI
    // ---------------------------------------------------------------

    public void aggiungiCampoComune(String nome, boolean obbligatorio)
    {
        nome = normalizza(nome);

        if (nomeCampoGiaEsistente(nome))
            throw new IllegalArgumentException("Esiste già un campo con questo nome.");

        data.getCampiComuni().add(new Campo(nome, TipoCampo.COMUNE, obbligatorio));
        sortCampiComuni();
        db.save(data);
    }

    public boolean rimuoviCampoComune(String nome)
    {
        final String n = normalizza(nome);
        boolean rimosso = data.getCampiComuni().removeIf(c -> c.getNome().equalsIgnoreCase(n));

        if (rimosso)
            db.save(data);

        return rimosso;
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

    public void creaCategoria(String nomeCategoria)
    {
        nomeCategoria = normalizza(nomeCategoria);

        if (data.findCategoria(nomeCategoria) != null)
            throw new IllegalArgumentException("Categoria già esistente.");

        Categoria cat = new Categoria(nomeCategoria);
        data.getCategorie().add(cat);
        sortCategorie();
        db.save(data);
    }

    public boolean rimuoviCategoria(String nomeCategoria)
    {
        final String n = normalizza(nomeCategoria);
        boolean rimossa = data.getCategorie().removeIf(c -> c.getNome().equalsIgnoreCase(n));

        if (rimossa)
            db.save(data);

        return rimossa;
    }

    public List<Categoria> getCategorie()
    {
        return Collections.unmodifiableList(data.getCategorie());
    }

    public Categoria getCategoria(String nomeCategoria)
    {
        return getCategoriaOrThrow(nomeCategoria);
    }

    public Categoria getCategoriaOrThrow(String nomeCategoria)
    {
        nomeCategoria = normalizza(nomeCategoria);
        Categoria c = data.findCategoria(nomeCategoria);

        if (c == null)
            throw new IllegalArgumentException("Categoria non trovata: \"" + nomeCategoria + "\".");

        return c;
    }

    // ---------------------------------------------------------------
    // CAMPI SPECIFICI
    // ---------------------------------------------------------------

    public void aggiungiCampoSpecifico(String nomeCategoria, String nomeCampo, boolean obbligatorio)
    {
        nomeCampo = normalizza(nomeCampo);

        if (nomeCampoGiaEsistente(nomeCampo))
            throw new IllegalArgumentException("Esiste già un campo con questo nome.");

        Categoria c = getCategoriaOrThrow(nomeCategoria);
        c.addCampoSpecifico(new Campo(nomeCampo, TipoCampo.SPECIFICO, obbligatorio));
        db.save(data);
    }

    public boolean rimuoviCampoSpecifico(String nomeCategoria, String nomeCampo)
    {
        nomeCampo = normalizza(nomeCampo);
        Categoria c = getCategoriaOrThrow(nomeCategoria);
        boolean rimosso = c.removeCampoSpecifico(nomeCampo);

        if (rimosso)
            db.save(data);

        return rimosso;
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

    private void sortCampiComuni()
    {
        data.getCampiComuni().sort(Comparator.comparing(c -> c.getNome().toLowerCase()));
    }

    private void sortCategorie()
    {
        data.getCategorie().sort(Comparator.comparing(c -> c.getNome().toLowerCase()));
    }

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
        List<Campo> tutti = new ArrayList<>();
        tutti.addAll(data.getCampiBase());
        tutti.addAll(data.getCampiComuni());

        for (Categoria cat : data.getCategorie())
            tutti.addAll(cat.getCampiSpecifici());

        return tutti;
    }

    private String normalizza(String s)
    {
        if (s == null)
            throw new IllegalArgumentException("Nome non valido (null).");

        return s.trim();
    }
}