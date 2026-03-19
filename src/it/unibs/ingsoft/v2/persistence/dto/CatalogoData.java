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
 * JSON-serializable snapshot of the field/category catalogue.
 */
public final class CatalogoData
{
    private final List<Campo>     campiBase;
    private boolean               campiBaseFissati;
    private final List<Campo>     campiComuni;
    private final List<Categoria> categorie;

    public CatalogoData()
    {
        this.campiBase        = new ArrayList<>();
        this.campiBaseFissati = false;
        this.campiComuni      = new ArrayList<>();
        this.categorie        = new ArrayList<>();
    }

    /** Jackson deserialisation factory. */
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

    // ---- base fields ----

    public List<Campo> getCampiBase()       { return Collections.unmodifiableList(campiBase); }
    public boolean isCampiBaseFissati()     { return campiBaseFissati; }
    public void setCampiBaseFissati(boolean value) { this.campiBaseFissati = value; }
    public void addCampoBase(Campo c)       { campiBase.add(c); }

    // ---- common fields ----

    public List<Campo> getCampiComuni()     { return Collections.unmodifiableList(campiComuni); }
    public void addCampoComune(Campo c)     { campiComuni.add(c); }

    public boolean removeCampoComune(String nome)
    {
        return campiComuni.removeIf(c -> c.getNome().equalsIgnoreCase(nome));
    }

    // ---- categories ----

    public List<Categoria> getCategorie()   { return Collections.unmodifiableList(categorie); }
    public void addCategoria(Categoria cat) { categorie.add(cat); }

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
