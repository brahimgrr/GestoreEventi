//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//



import java.io.Serializable;
import java.util.Objects;

public final class Campo implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String nome;
    private final FieldScope tipo;
    private boolean obbligatorio;

    public Campo(String nome, FieldScope tipo, boolean obbligatorio) {
        if (nome != null && !nome.isBlank()) {
            this.nome = nome.trim();
            this.tipo = (FieldScope)Objects.requireNonNull(tipo, "Tipo nullo.");
            this.obbligatorio = obbligatorio;
        } else {
            throw new IllegalArgumentException("Nome campo non valido.");
        }
    }

    public String getNome() {
        return this.nome;
    }

    public FieldScope getScope() {
        return this.tipo;
    }

    public boolean isObbligatorio() {
        return this.obbligatorio;
    }

    public void setObbligatorio(boolean obbligatorio) {
        this.obbligatorio = obbligatorio;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Campo)) {
            return false;
        } else {
            Campo campo = (Campo)o;
            return this.nome.equalsIgnoreCase(campo.nome) && this.tipo == campo.tipo;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.nome.toLowerCase(), this.tipo});
    }

    public String toString() {
        String var10000 = this.nome;
        return var10000 + " [" + String.valueOf(this.tipo) + "]" + (this.obbligatorio ? " (obbligatorio)" : " (facoltativo)");
    }
}
