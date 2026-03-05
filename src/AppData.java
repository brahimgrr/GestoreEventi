import java.io.Serializable;
import java.util.*;

public final class AppData implements Serializable
{
    private static final long serialVersionUID = 1L;

    // Mappa con username e password (NON SICURA ma sicurezza non richiesta)
    private final Map<String, String> configuratori = new HashMap<>();

    // Campi base: immutabili una volta fissati
    private final List<Campo> campiBase = new ArrayList<>();
    private boolean campiBaseFissati = false;

    // Campi comuni: modificabili
    private final List<Campo> campiComuni = new ArrayList<>();

    // Categorie
    private final List<Categoria> categorie = new ArrayList<>();

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

    public Categoria findCategoria(String nome)
    {
        return categorie.stream()
                        .filter(c -> c.getNome().equalsIgnoreCase(nome))
                        .findFirst()
                        .orElse(null);
    }
}
