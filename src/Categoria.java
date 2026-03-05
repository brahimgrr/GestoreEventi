//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//



import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class Categoria implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String nome;
    private final List<Campo> campiSpecifici = new ArrayList();

    public Categoria(String nome) {
        if (nome != null && !nome.isBlank()) {
            this.nome = nome.trim();
        } else {
            throw new IllegalArgumentException("Nome categoria non valido.");
        }
    }

    public String getNome() {
        return this.nome;
    }

    public List<Campo> getCampiSpecifici() {
        return Collections.unmodifiableList(this.campiSpecifici);
    }

    public void addCampoSpecifico(Campo campoSpecifico) {
        Objects.requireNonNull(campoSpecifico, "Campo nullo.");
        if (campoSpecifico.getScope() != FieldScope.SPECIFICO) {
            throw new IllegalArgumentException("Il campo deve avere scope SPECIFICO.");
        } else if (this.containsCampo(campoSpecifico.getNome())) {
            throw new IllegalArgumentException("Esiste già un campo specifico con questo nome nella categoria.");
        } else {
            this.campiSpecifici.add(campoSpecifico);
            this.campiSpecifici.sort(Comparator.comparing((c) -> c.getNome().toLowerCase()));
        }
    }

    public boolean removeCampoSpecifico(String nomeCampo) {
        return this.campiSpecifici.removeIf((c) -> c.getNome().equalsIgnoreCase(nomeCampo));
    }

    public boolean setObbligatorietaCampoSpecifico(String nomeCampo, boolean obbligatorio) {
        for(Campo c : this.campiSpecifici) {
            if (c.getNome().equalsIgnoreCase(nomeCampo)) {
                c.setObbligatorio(obbligatorio);
                return true;
            }
        }

        return false;
    }

    public boolean containsCampo(String nomeCampo) {
        for(Campo c : this.campiSpecifici) {
            if (c.getNome().equalsIgnoreCase(nomeCampo)) {
                return true;
            }
        }

        return false;
    }

    public String toString() {
        String var10000 = this.nome;
        return "Categoria{nome='" + var10000 + "', campiSpecifici=" + String.valueOf(this.campiSpecifici) + "}";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Categoria)) {
            return false;
        } else {
            Categoria categoria = (Categoria)o;
            return this.nome.equalsIgnoreCase(categoria.nome);
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.nome.toLowerCase()});
    }
}
