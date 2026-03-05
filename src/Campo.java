import java.io.Serializable;
import java.util.Objects;

public final class Campo implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final String nome;
    private final TipoCampo tipo;
    private boolean obbligatorio;

    public Campo(String nome, TipoCampo tipo, boolean obbligatorio)
    {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome campo non valido.");

        this.nome = nome.trim();
        this.tipo = Objects.requireNonNull(tipo, "Tipo nullo.");
        this.obbligatorio = obbligatorio;
    }

    public String getNome()
    {
        return nome;
    }

    public TipoCampo getScope()
    {
        return tipo;
    }

    public boolean isObbligatorio()
    {
        return obbligatorio;
    }

    public void setObbligatorio(boolean obbligatorio)
    {
        this.obbligatorio = obbligatorio;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (!(o instanceof Campo))
            return false;

        Campo campo = (Campo) o;
        return (nome.equalsIgnoreCase(campo.nome) && tipo == campo.tipo);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(nome.toLowerCase(), tipo);
    }

    @Override
    public String toString()
    {
        return nome + " [" + tipo + "]" + (obbligatorio ? " (obbligatorio)" : " (facoltativo)");
    }
}