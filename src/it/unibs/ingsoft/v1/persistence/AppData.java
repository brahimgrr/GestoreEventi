package it.unibs.ingsoft.v1.persistence;

import it.unibs.ingsoft.v1.model.Campo;
import it.unibs.ingsoft.v1.model.Categoria;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public final class AppData implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    // ---------------------------------------------------------------
    // DATI UTENTI
    // ---------------------------------------------------------------

    /** Mappa username → password dei configuratori registrati (sicurezza non richiesta). */
    private final Map<String, String> configuratori = new HashMap<>();

    // ---------------------------------------------------------------
    // CAMPI
    // ---------------------------------------------------------------

    private final List<Campo> campiBase   = new ArrayList<>();
    private boolean           campiBaseFissati = false;

    private final List<Campo> campiComuni = new ArrayList<>();

    // ---------------------------------------------------------------
    // CATEGORIE
    // ---------------------------------------------------------------

    private final List<Categoria> categorie = new ArrayList<>();

    // ---------------------------------------------------------------
    // GETTER / SETTER
    // ---------------------------------------------------------------

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

    // ---------------------------------------------------------------
    // UTILITY
    // ---------------------------------------------------------------

    public Categoria findCategoria(String nome)
    {
        return categorie.stream()
                .filter(c -> c.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .orElse(null);
    }
}