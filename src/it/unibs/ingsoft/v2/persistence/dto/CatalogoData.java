package it.unibs.ingsoft.v2.persistence.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.unibs.ingsoft.v2.domain.Campo;
import it.unibs.ingsoft.v2.domain.Categoria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Pure serializable DTO for the field/category catalogue.
 * Contains no domain query logic — all business operations live in the service layer.
 * Structural mutations (add/remove/replace) are deliberately kept here so the
 * service does not need to rebuild the full list on every change.
 */
public final class CatalogoData {
    private final List<Campo>     campiBase;
    private boolean         campiBaseFissati;
    private final List<Campo>     campiComuni;
    private final List<Categoria> categorie;

    public CatalogoData()
    {
        this.campiBase        = new ArrayList<>();
        this.campiBaseFissati = false;
        this.campiComuni      = new ArrayList<>();
        this.categorie        = new ArrayList<>();
    }

    /** Jackson deserialization factory. */
    @JsonCreator
    public static CatalogoData fromJson(
            @JsonProperty("campiBase")        List<Campo>     campiBase,
            @JsonProperty("campiBaseFissati") boolean         campiBaseFissati,
            @JsonProperty("campiComuni")      List<Campo>     campiComuni,
            @JsonProperty("categorie")        List<Categoria> categorie)
    {
        CatalogoData d = new CatalogoData();
        if (campiBase   != null) d.campiBase.addAll(campiBase);
        if (campiBaseFissati)    d.campiBaseFissati = true;
        if (campiComuni != null) d.campiComuni.addAll(campiComuni);
        if (categorie   != null) d.categorie.addAll(categorie);
        return d;
    }

    // ---------------------------------------------------------------
    // CAMPI BASE
    // ---------------------------------------------------------------

    public List<Campo> getCampiBase() {
        return Collections.unmodifiableList(campiBase);
    }

    public boolean isCampiBaseFissati() {
        return campiBaseFissati;
    }

    /** One-way: once base fields are marked as fixed they cannot be un-fixed. */
    public void setCampiBaseFissati()
    {
        this.campiBaseFissati = true;
    }

    /** Guarded: throws if base fields are already fixed. */
    public void addCampoBase(Campo c)
    {
        if (campiBaseFissati)
            throw new IllegalStateException("I campi base sono già fissati e immutabili.");
        campiBase.add(c);
    }

    /** Guarded: throws if base fields are already fixed. */
    public void clearCampiBase()
    {
        if (campiBaseFissati)
            throw new IllegalStateException("I campi base sono già fissati e immutabili.");
        campiBase.clear();
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
    }

    public boolean removeCampoComune(String nome)
    {
        return campiComuni.removeIf(c -> c.getNome().equalsIgnoreCase(nome));
    }

    /**
     * Replaces the common field whose name matches {@code nome} (case-insensitive)
     * with {@code nuovoCampo}. Used to update the {@code obbligatorio} flag immutably.
     *
     * @return true if found and replaced, false otherwise
     */
    public boolean replaceCampoComune(String nome, Campo nuovoCampo)
    {
        for (int i = 0; i < campiComuni.size(); i++)
        {
            if (campiComuni.get(i).getNome().equalsIgnoreCase(nome))
            {
                campiComuni.set(i, nuovoCampo);
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------
    // CATEGORIE
    // ---------------------------------------------------------------

    public List<Categoria> getCategorie() {
        return Collections.unmodifiableList(categorie);
    }

    public void addCategoria(Categoria c) {
        categorie.add(c);
    }

    public boolean removeCategoria(String nome)
    {
        return categorie.removeIf(c -> c.getNome().equalsIgnoreCase(nome));
    }

    public Optional<Categoria> findCategoria(String nome)
    {
        if (nome == null) return Optional.empty();
        return categorie.stream()
                .filter(c -> c.getNome().equalsIgnoreCase(nome.trim()))
                .findFirst();
    }
}
