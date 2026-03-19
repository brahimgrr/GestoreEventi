package it.unibs.ingsoft.v3.persistence;

import it.unibs.ingsoft.v3.model.Campo;
import it.unibs.ingsoft.v3.model.Categoria;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * Pure serializable DTO for the category catalogue (fields + categories).
 * No sorting side-effects; ordering is a service/view concern.
 */
public final class CatalogoData implements Serializable
{
    @Serial private static final long serialVersionUID = 1L;

    private final List<Campo>     campiBase       = new ArrayList<>();
    private boolean               campiBaseFissati = false;
    private final List<Campo>     campiComuni     = new ArrayList<>();
    private final List<Categoria> categorie       = new ArrayList<>();

    public List<Campo> getCampiBase()
    {
        return Collections.unmodifiableList(campiBase);
    }

    public void addCampoBase(Campo c)
    {
        campiBase.add(c);
    }

    public boolean isCampiBaseFissati()
    {
        return campiBaseFissati;
    }

    public void setCampiBaseFissati(boolean value)
    {
        this.campiBaseFissati = value;
    }

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

    public List<Categoria> getCategorie()
    {
        return Collections.unmodifiableList(categorie);
    }

    public void addCategoria(Categoria c)
    {
        categorie.add(c);
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
}
