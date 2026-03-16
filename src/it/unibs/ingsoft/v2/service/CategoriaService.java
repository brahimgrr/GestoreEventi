package it.unibs.ingsoft.v2.service;

import it.unibs.ingsoft.v2.model.*;
import it.unibs.ingsoft.v2.persistence.AppData;
import it.unibs.ingsoft.v2.persistence.DatabaseService;

import java.util.*;

public final class CategoriaService
{
    private final DatabaseService db;
    private final AppData data;

    public CategoriaService(DatabaseService db, AppData data)
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
                data.getCampiBase().add(new Campo(cbd.getNomeCampo(), TipoCampo.BASE, cbd.getTipoDato(), true));
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

            data.getCampiBase().add(new Campo(nome, TipoCampo.BASE, td, true));
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

    public void aggiungiCampoComune(String nome, TipoDato tipoDato, boolean obbligatorio)
    {
        nome = normalizza(nome);

        if (nomeCampoGiaEsistente(nome))
            throw new IllegalArgumentException("Esiste già un campo con il nome: \"" + nome + "\".");

        data.getCampiComuni().add(new Campo(nome, TipoCampo.COMUNE, tipoDato, obbligatorio));
        sortCampiComuni();
        db.save(data);
    }

    public boolean rimuoviCampoComune(String nome)
    {
        final String n = normalizza(nome);
        boolean rimosso = data.getCampiComuni().removeIf(c -> c.getNome().equalsIgnoreCase(n));
        if (rimosso) db.save(data);
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
        boolean rimossa = data.getCategorie()
                .removeIf(c -> c.getNome().equalsIgnoreCase(n));

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

    public void aggiungiCampoSpecifico(String nomeCategoria, String nomeCampo, TipoDato tipoDato, boolean obbligatorio)
    {
        nomeCampo = normalizza(nomeCampo);

        if (nomeCampoGiaEsistente(nomeCampo))
            throw new IllegalArgumentException("Esiste già un campo con il nome: \"" + nomeCampo + "\".");

        Categoria c = getCategoriaOrThrow(nomeCategoria);
        c.addCampoSpecifico(new Campo(nomeCampo, TipoCampo.SPECIFICO, tipoDato, obbligatorio));
        db.save(data);
    }

    public boolean rimuoviCampoSpecifico(String nomeCategoria, String nomeCampo)
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
        List<Campo> all = new ArrayList<>();
        all.addAll(data.getCampiBase());
        all.addAll(data.getCampiComuni());

        for (Categoria c : data.getCategorie())
            all.addAll(c.getCampiSpecifici());

        return all;
    }

    private String normalizza(String s)
    {
        if (s == null) throw new IllegalArgumentException("Nome non valido (null).");
        return s.trim();
    }
}