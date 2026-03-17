package it.unibs.ingsoft.v5.persistence;

import it.unibs.ingsoft.v5.model.Campo;
import it.unibs.ingsoft.v5.model.Categoria;
import it.unibs.ingsoft.v5.model.Notifica;
import it.unibs.ingsoft.v5.model.Proposta;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public final class AppData implements Serializable
{
    private static final long serialVersionUID = 1L;

    // NUOVO V3
    private Map<String, String>         fruitori  = new HashMap<>();
    private Map<String, List<Notifica>> notifiche = new HashMap<>();

    // configuratori registrati
    private final Map<String, String> configuratori = new HashMap<>();

    // campi base
    private final List<Campo> campiBase      = new ArrayList<>();
    private boolean           campiBaseFissati = false;

    // campi comuni
    private final List<Campo> campiComuni = new ArrayList<>();

    // categorie
    private final List<Categoria> categorie = new ArrayList<>();

    // archivio delle proposte pubblicate
    private final List<Proposta> proposte = new ArrayList<>();

    // ---------------------------------------------------------------
    // CONFIGURATORI
    // ---------------------------------------------------------------

    public Map<String, String> getConfiguratori()
    {
        return Collections.unmodifiableMap(configuratori);
    }

    public void addConfiguratore(String username, String password)
    {
        configuratori.put(username, password);
    }

    // ---------------------------------------------------------------
    // FRUITORI
    // ---------------------------------------------------------------

    public Map<String, String> getFruitori()
    {
        return Collections.unmodifiableMap(fruitori);
    }

    public void addFruitore(String username, String password)
    {
        fruitori.put(username, password);
    }

    // ---------------------------------------------------------------
    // NOTIFICHE
    // ---------------------------------------------------------------

    public Map<String, List<Notifica>> getNotifiche()
    {
        return Collections.unmodifiableMap(notifiche);
    }

    public void addNotifica(String username, Notifica n)
    {
        notifiche.computeIfAbsent(username, k -> new ArrayList<>()).add(n);
    }

    // ---------------------------------------------------------------
    // CAMPI BASE
    // ---------------------------------------------------------------

    public List<Campo> getCampiBase()
    {
        return Collections.unmodifiableList(campiBase);
    }

    public boolean isCampiBaseFissati()
    {
        return campiBaseFissati;
    }

    public void setCampiBaseFissati(boolean campiBaseFissati)
    {
        this.campiBaseFissati = campiBaseFissati;
    }

    public void addCampoBase(Campo c)
    {
        campiBase.add(c);
    }

    // ---------------------------------------------------------------
    // CAMPI COMUNI
    // ---------------------------------------------------------------

    public List<Campo> getCampiComuni()
    {
        return Collections.unmodifiableList(campiComuni);
    }

    public void addCampoComune(Campo c)
    {
        campiComuni.add(c);
        campiComuni.sort(Comparator.comparing(cc -> cc.getNome().toLowerCase()));
    }

    public boolean removeCampoComune(String nome)
    {
        return campiComuni.removeIf(c -> c.getNome().equalsIgnoreCase(nome));
    }

    // ---------------------------------------------------------------
    // CATEGORIE
    // ---------------------------------------------------------------

    public List<Categoria> getCategorie()
    {
        return Collections.unmodifiableList(categorie);
    }

    public void addCategoria(Categoria c)
    {
        categorie.add(c);
        categorie.sort(Comparator.comparing(cc -> cc.getNome().toLowerCase()));
    }

    public boolean removeCategoria(String nome)
    {
        return categorie.removeIf(c -> c.getNome().equalsIgnoreCase(nome));
    }

    public Optional<Categoria> findCategoria(String nome)
    {
        return categorie.stream()
                .filter(c -> c.getNome().equalsIgnoreCase(nome))
                .findFirst();
    }

    // ---------------------------------------------------------------
    // PROPOSTE
    // ---------------------------------------------------------------

    public List<Proposta> getProposte()
    {
        return Collections.unmodifiableList(proposte);
    }

    public void addProposta(Proposta p)
    {
        proposte.add(p);
    }

    // ---------------------------------------------------------------
    // SERIALIZATION SAFETY
    // ---------------------------------------------------------------

    @Serial
    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        // Safety net: initialize any fields that may be null
        // due to deserialization of older saved data
        if (fruitori == null)
            fruitori = new HashMap<>();

        if (notifiche == null)
            notifiche = new HashMap<>();
    }
}
