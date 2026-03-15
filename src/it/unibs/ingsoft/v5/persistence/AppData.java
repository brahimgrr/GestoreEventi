package it.unibs.ingsoft.v5.persistence;

import it.unibs.ingsoft.v5.model.Campo;
import it.unibs.ingsoft.v5.model.Categoria;
import it.unibs.ingsoft.v5.model.Notifica;
import it.unibs.ingsoft.v5.model.Proposta;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AppData implements Serializable
{
    private static final long serialVersionUID = 1L;

    // NUOVO V3
    private  Map<String, String>         fruitori    = new HashMap<>();
    private  Map<String, List<Notifica>> notifiche   = new HashMap<>();

    // configuratori registrati
    private final Map<String, String> configuratori = new HashMap<>();

    // campi base
    private final List<Campo> campiBase = new ArrayList<>();
    private boolean campiBaseFissati = false;

    // campi comuni
    private final List<Campo> campiComuni = new ArrayList<>();

    // categorie
    private final List<Categoria> categorie = new ArrayList<>();

    // NUOVO V3
    public Map<String, String> getFruitori()
    {
        return fruitori;
    }

    public Map<String, List<Notifica>> getNotifiche()
    {
        return notifiche;
    }

    /**
     * Returns the notification list for a specific fruitore.
     * Creates an empty list if none exists yet.
     */
    public List<Notifica> getNotifichePerFruitore(String username)
    {
        return notifiche.computeIfAbsent(username, k -> new ArrayList<>());
    }

    // archivio delle proposte pubblicate
    private final List<Proposta> proposte = new ArrayList<>();

    public Map<String, String> getConfiguratori()
    {
        return configuratori;
    }

    public List<Campo> getCampiBase()
    {
        return campiBase;
    }

    public boolean isCampiBaseFissati()
    {
        return campiBaseFissati;
    }

    public void setCampiBaseFissati(boolean campiBaseFissati)
    {
        this.campiBaseFissati = campiBaseFissati;
    }

    public List<Campo> getCampiComuni()
    {
        return campiComuni;
    }

    public List<Categoria> getCategorie()
    {
        return categorie;
    }

    public List<Proposta> getProposte()
    {
        return proposte;
    }

    public Categoria findCategoria(String nome)
    {
        return categorie.stream()
                .filter(c -> c.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .orElse(null);
    }

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

    /*
    public Proposta findProposta(String titolo)

    {
        return proposte.stream()
                .filter(p -> p.getTitolo().equalsIgnoreCase(titolo))
                .findFirst()
                .orElse(null);
    }
    */
}