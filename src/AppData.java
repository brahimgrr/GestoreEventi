//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AppData implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<String, String> configuratori = new HashMap();
    private final List<Campo> campiBase = new ArrayList();
    private boolean campiBaseFissati = false;
    private final List<Campo> campiComuni = new ArrayList();
    private final List<Categoria> categorie = new ArrayList();

    public Map<String, String> getConfiguratori() {
        return this.configuratori;
    }

    public List<Campo> getCampiBase() {
        return this.campiBase;
    }

    public boolean isCampiBaseFissati() {
        return this.campiBaseFissati;
    }

    public void setCampiBaseFissati(boolean campiBaseFissati) {
        this.campiBaseFissati = campiBaseFissati;
    }

    public List<Campo> getCampiComuni() {
        return this.campiComuni;
    }

    public List<Categoria> getCategorie() {
        return this.categorie;
    }

    public Categoria findCategoria(String nome) {
        return (Categoria)this.categorie.stream().filter((c) -> c.getNome().equalsIgnoreCase(nome)).findFirst().orElse((Categoria) null);
    }
}
