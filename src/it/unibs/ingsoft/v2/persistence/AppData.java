package it.unibs.ingsoft.v2.persistence;

import it.unibs.ingsoft.v2.model.Campo;
import it.unibs.ingsoft.v2.model.Categoria;
import it.unibs.ingsoft.v2.model.Proposta;

import java.io.Serializable;
import java.util.*;

public final class AppData implements Serializable
{
    private static final long serialVersionUID = 1L;

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
}
