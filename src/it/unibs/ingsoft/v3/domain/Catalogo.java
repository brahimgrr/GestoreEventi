package it.unibs.ingsoft.v3.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * JSON-serializable DTO for the category catalogue (fields + categories).
 * No sorting side-effects; ordering is a service/view concern.
 */
public final class Catalogo
{
    private final List<Campo>     campiBase        = new ArrayList<>();
    private boolean               campiBaseFissati = false;
    private final List<Campo>     campiComuni      = new ArrayList<>();
    private final List<Categoria> categorie        = new ArrayList<>();

    public Catalogo() {}

    /** Jackson deserialisation factory. */
    @JsonCreator
    public static Catalogo fromJson(
            @JsonProperty("campiBase")        List<Campo>     campiBase,
            @JsonProperty("campiBaseFissati") boolean         campiBaseFissati,
            @JsonProperty("campiComuni")      List<Campo>     campiComuni,
            @JsonProperty("categorie")        List<Categoria> categorie)
    {
        Catalogo d = new Catalogo();
        if (campiBase   != null) d.campiBase.addAll(campiBase);
        if (campiBaseFissati)    d.campiBaseFissati = true;
        if (campiComuni != null) d.campiComuni.addAll(campiComuni);
        if (categorie   != null) d.categorie.addAll(categorie);
        return d;
    }

    public List<Campo> getCampiBase()              { return Collections.unmodifiableList(campiBase); }
    public boolean     isCampiBaseFissati()        { return campiBaseFissati; }
    public void        setCampiBaseFissati(boolean v) { this.campiBaseFissati = v; }
    public void        addCampoBase(Campo c)       { campiBase.add(c); }

    public List<Campo> getCampiComuni()            { return Collections.unmodifiableList(campiComuni); }
    public void        addCampoComune(Campo c)     { campiComuni.add(c); }

    public boolean removeCampoComune(String nome)
    {
        return campiComuni.removeIf(c -> c.getNome().equalsIgnoreCase(nome));
    }

    /**
     * Replaces the common field whose name matches {@code nome} (case-insensitive)
     * with {@code nuovoCampo}. Used to update the {@code obbligatorio} flag immutably.
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

    public List<Categoria> getCategorie()          { return Collections.unmodifiableList(categorie); }
    public void            addCategoria(Categoria c) { categorie.add(c); }

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
}
