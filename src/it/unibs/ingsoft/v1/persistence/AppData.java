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

    // Mappa con username e password (NON SICURA ma sicurezza non richiesta)
    private final Map<String, String> configuratori = new HashMap<>();

    // Campi base: immutabili una volta fissati
    private final List<Campo> campiBase      = new ArrayList<>();
    private boolean           campiBaseFissati = false;

    // Campi comuni: modificabili
    private final List<Campo> campiComuni = new ArrayList<>();

    // Categorie
    private final List<Categoria> categorie = new ArrayList<>();

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

    public void clearCampiBase()
    {
        campiBase.clear();
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
}
