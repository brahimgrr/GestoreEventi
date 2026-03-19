package it.unibs.ingsoft.v4.persistence;

import it.unibs.ingsoft.v4.model.Campo;
import it.unibs.ingsoft.v4.model.Categoria;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Pure serializable DTO holding the catalogue data:
 * base fields, common fields, categories and their specific fields.
 * No sorting or business logic — that belongs in the service layer.
 */
public final class CatalogoData implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final List<Campo>     campiBase   = new ArrayList<>();
    private final List<Campo>     campiComuni = new ArrayList<>();
    private final List<Categoria> categorie   = new ArrayList<>();
    private boolean campiBaseFissati = false;

    public List<Campo> getCampiBase()
    {
        return Collections.unmodifiableList(campiBase);
    }

    public void addCampoBase(Campo c)
    {
        campiBase.add(c);
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

    public void addCategoria(Categoria cat)
    {
        categorie.add(cat);
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

    public boolean isCampiBaseFissati()
    {
        return campiBaseFissati;
    }

    public void setCampiBaseFissati(boolean value)
    {
        this.campiBaseFissati = value;
    }
}
