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
    private final AppData data;

    public CategoriaService(DatabaseService db, AppData data)
    {
        this.db = Objects.requireNonNull(db);
        this.data = Objects.requireNonNull(data);
    }

    // ---------- Campi base ----------
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

        data.getCampiBase().clear();

        for (String n : clean)
            data.getCampiBase().add(new Campo(n, TipoCampo.BASE, true)); // tutti obbligatori

        //data.getCampiBase().sort(Comparator.comparing(c -> c.getNome().toLowerCase()));
        data.setCampiBaseFissati(true);
        db.save(data);
    }

    public List<Campo> getCampiBase()
    {
        return Collections.unmodifiableList(data.getCampiBase());
    }

    // ---------- Campi comuni ----------
    public void addCampoComune(String nome, boolean obbligatorio)
    {
        if (nomeCampoGiaEsistente(nome))
            throw new IllegalArgumentException("Esiste già un campo con questo nome.");

        Campo c = new Campo(nome, TipoCampo.COMUNE, obbligatorio);
        data.getCampiComuni().add(c);
        sortCampiComuni();
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

    private void sortCampiComuni()
    {
        data.getCampiComuni().sort(Comparator.comparing(c -> c.getNome().toLowerCase()));
    }

    public boolean removeCampoComune(String nome)
    {
        boolean removed = data.getCampiComuni().removeIf(c -> c.getNome().equalsIgnoreCase(nome));

        if (removed)
            db.save(data);

        return removed;
    }

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
    public Categoria createCategoria(String nomeCategoria)
    {
        if (data.findCategoria(nomeCategoria) != null)
            throw new IllegalArgumentException("it.unibs.ingsoft.v1.model.Categoria già esistente.");

        Categoria cat = new Categoria(nomeCategoria);
        data.getCategorie().add(cat);
        sortCategorie();
        db.save(data);
        return cat;
    }

    public boolean removeCategoria(String nomeCategoria)
    {
        boolean removed = data.getCategorie().removeIf(c -> c.getNome().equalsIgnoreCase(nomeCategoria));

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

    public Categoria getCategoria(String nomeCategoria)
    {
        return getCategoriaOrThrow(nomeCategoria);
    }

    public Categoria getCategoriaOrThrow(String nomeCategoria)
    {
        if (data.findCategoria(nomeCategoria) == null)
        {
            throw new IllegalArgumentException("it.unibs.ingsoft.v1.model.Categoria non trovata.");
        }

        return data.findCategoria(nomeCategoria);
    }

    public void addCampoSpecifico(String nomeCategoria, String nomeCampo, boolean obbligatorio)
    {
        if (nomeCampoGiaEsistente(nomeCampo))
            throw new IllegalArgumentException("Esiste già un campo con questo nome.");

        Categoria c = getCategoriaOrThrow(nomeCategoria);
        c.addCampoSpecifico(new Campo(nomeCampo, TipoCampo.SPECIFICO, obbligatorio));
        db.save(data);
    }

    public boolean removeCampoSpecifico(String nomeCategoria, String nomeCampo)
    {
        Categoria c = getCategoriaOrThrow(nomeCategoria);
        boolean removed = c.removeCampoSpecifico(nomeCampo);

        if (removed)
            db.save(data);

        return removed;
    }

    public boolean setObbligatorietaCampoSpecifico(String nomeCategoria, String nomeCampo, boolean obbligatorio)
    {
        Categoria c = getCategoriaOrThrow(nomeCategoria);
        boolean ok = c.setObbligatorietaCampoSpecifico(nomeCampo, obbligatorio);

        if (ok)
            db.save(data);

        return ok;
    }

    private void sortCategorie()
    {
        data.getCategorie().sort(Comparator.comparing(c -> c.getNome().toLowerCase()));
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