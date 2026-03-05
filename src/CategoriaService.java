//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class CategoriaService {
    private final DatabaseService db;
    private final AppData data;

    public CategoriaService(DatabaseService db, AppData data) {
        this.db = (DatabaseService)Objects.requireNonNull(db);
        this.data = (AppData)Objects.requireNonNull(data);
    }

    public void fissareCampiBase(List<String> nomiCampiBase) {
        if (this.data.isCampiBaseFissati()) {
            throw new IllegalStateException("I campi base sono già stati fissati e sono immutabili.");
        } else {
            Objects.requireNonNull(nomiCampiBase);
            List<String> cleaned = new ArrayList();

            for(String s : nomiCampiBase) {
                if (s != null && !s.isBlank()) {
                    cleaned.add(s.trim());
                }
            }

            if (cleaned.isEmpty()) {
                throw new IllegalArgumentException("Lista campi base vuota.");
            } else {
                Set<String> set = new HashSet();

                for(String n : cleaned) {
                    String key = n.toLowerCase();
                    if (!set.add(key)) {
                        throw new IllegalArgumentException("Nome campo base duplicato: " + n);
                    }
                }

                this.data.getCampiBase().clear();

                for(String n : cleaned) {
                    this.data.getCampiBase().add(new Campo(n, FieldScope.BASE, true));
                }

                this.data.getCampiBase().sort(Comparator.comparing((c) -> c.getNome().toLowerCase()));
                this.data.setCampiBaseFissati(true);
                this.db.save(this.data);
            }
        }
    }

    public List<Campo> getCampiBase() {
        return Collections.unmodifiableList(this.data.getCampiBase());
    }

    public void addCampoComune(String nome, boolean obbligatorio) {
        Campo c = new Campo(nome, FieldScope.COMUNE, obbligatorio);
        this.assicuraCampoComuneUnico(nome);
        this.data.getCampiComuni().add(c);
        this.sortCampiComuni();
        this.db.save(this.data);
    }

    public boolean removeCampoComune(String nome) {
        boolean removed = this.data.getCampiComuni().removeIf((c) -> c.getNome().equalsIgnoreCase(nome));
        if (removed) {
            this.db.save(this.data);
        }

        return removed;
    }

    public boolean setObbligatorietaCampoComune(String nome, boolean obbligatorio) {
        for(Campo c : this.data.getCampiComuni()) {
            if (c.getNome().equalsIgnoreCase(nome)) {
                c.setObbligatorio(obbligatorio);
                this.db.save(this.data);
                return true;
            }
        }

        return false;
    }

    public List<Campo> getCampiComuni() {
        return Collections.unmodifiableList(this.data.getCampiComuni());
    }

    private void assicuraCampoComuneUnico(String nome) {
        for(Campo c : this.data.getCampiComuni()) {
            if (c.getNome().equalsIgnoreCase(nome)) {
                throw new IllegalArgumentException("Esiste già un campo comune con questo nome.");
            }
        }

    }

    private void sortCampiComuni() {
        this.data.getCampiComuni().sort(Comparator.comparing((c) -> c.getNome().toLowerCase()));
    }

    public Categoria createCategoria(String nomeCategoria) {
        if (this.data.findCategoria(nomeCategoria) != null) {
            throw new IllegalArgumentException("Categoria già esistente.");
        } else {
            Categoria cat = new Categoria(nomeCategoria);
            this.data.getCategorie().add(cat);
            this.sortCategorie();
            this.db.save(this.data);
            return cat;
        }
    }

    public boolean removeCategoria(String nomeCategoria) {
        boolean removed = this.data.getCategorie().removeIf((c) -> c.getNome().equalsIgnoreCase(nomeCategoria));
        if (removed) {
            this.db.save(this.data);
        }

        return removed;
    }

    public List<Categoria> getCategorie() {
        return Collections.unmodifiableList(this.data.getCategorie());
    }

    public Categoria getCategoriaOrThrow(String nomeCategoria) {
        if (this.data.findCategoria(nomeCategoria) == null) {
            throw new IllegalArgumentException("Categoria non trovata.");
        } else {
            return this.data.findCategoria(nomeCategoria);
        }
    }

    public void addCampoSpecifico(String nomeCategoria, String nomeCampo, boolean obbligatorio) {
        Categoria c = this.getCategoriaOrThrow(nomeCategoria);
        c.addCampoSpecifico(new Campo(nomeCampo, FieldScope.SPECIFICO, obbligatorio));
        this.db.save(this.data);
    }

    public boolean removeCampoSpecifico(String nomeCategoria, String nomeCampo) {
        Categoria c = this.getCategoriaOrThrow(nomeCategoria);
        boolean removed = c.removeCampoSpecifico(nomeCampo);
        if (removed) {
            this.db.save(this.data);
        }

        return removed;
    }

    public boolean setObbligatorietaCampoSpecifico(String nomeCategoria, String nomeCampo, boolean obbligatorio) {
        Categoria c = this.getCategoriaOrThrow(nomeCategoria);
        boolean ok = c.setObbligatorietaCampoSpecifico(nomeCampo, obbligatorio);
        if (ok) {
            this.db.save(this.data);
        }

        return ok;
    }

    private void sortCategorie() {
        this.data.getCategorie().sort(Comparator.comparing((c) -> c.getNome().toLowerCase()));
    }

    public String renderSchemaPerCategoria(String nomeCategoria) {
        Categoria cat = this.getCategoriaOrThrow(nomeCategoria);
        StringBuilder sb = new StringBuilder();
        sb.append("Categoria: ").append(cat.getNome()).append("\n");
        sb.append("  Campi BASE (immutabili):\n");

        for(Campo c : this.data.getCampiBase()) {
            sb.append("   - ").append(c).append("\n");
        }

        sb.append("  Campi COMUNI:\n");
        if (this.data.getCampiComuni().isEmpty()) {
            sb.append("   (nessuno)\n");
        }

        for(Campo c : this.data.getCampiComuni()) {
            sb.append("   - ").append(c).append("\n");
        }

        sb.append("  Campi SPECIFICI:\n");
        if (cat.getCampiSpecifici().isEmpty()) {
            sb.append("   (nessuno)\n");
        }

        for(Campo c : cat.getCampiSpecifici()) {
            sb.append("   - ").append(c).append("\n");
        }

        return sb.toString();
    }
}
