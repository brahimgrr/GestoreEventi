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
    private final List<Campo> campiBase = new ArrayList<>();
    private boolean campiBaseFissati = false;

    // campi comuni
    private final List<Campo> campiComuni = new ArrayList<>();

    // categorie
    private final List<Categoria> categorie = new ArrayList<>();

    // ===== NUOVO (VERSIONE 2) =====
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