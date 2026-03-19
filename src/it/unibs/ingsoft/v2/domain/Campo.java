package it.unibs.ingsoft.v2.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a field definition (base, common, or category-specific).
 * Immutable except for the {@code obbligatorio} flag, which configurators may change.
 */
public final class Campo
{
    private final String    nome;
    private final TipoCampo tipo;
    private final TipoDato  tipoDato;
    private boolean         obbligatorio;

    /** Primary constructor — also used as the Jackson deserialisation entry point. */
    @JsonCreator
    public Campo(@JsonProperty("nome")         String    nome,
                 @JsonProperty("tipo")         TipoCampo tipo,
                 @JsonProperty("tipoDato")     TipoDato  tipoDato,
                 @JsonProperty("obbligatorio") boolean   obbligatorio)
    {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Il nome del campo non può essere vuoto.");
        if (tipo == null)
            throw new IllegalArgumentException("Il tipo del campo non può essere null.");
        if (tipoDato == null)
            throw new IllegalArgumentException("Il tipo dato del campo non può essere null.");

        this.nome         = nome.trim();
        this.tipo         = tipo;
        this.tipoDato     = tipoDato;
        this.obbligatorio = obbligatorio;
    }

    public String    getNome()        { return nome; }
    public TipoCampo getTipo()        { return tipo; }
    public TipoDato  getTipoDato()    { return tipoDato; }
    public boolean   isObbligatorio() { return obbligatorio; }

    public void setObbligatorio(boolean obbligatorio)
    {
        this.obbligatorio = obbligatorio;
    }

    /** Case-insensitive name + tipo equality. */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof Campo)) return false;
        Campo other = (Campo) obj;
        return nome.equalsIgnoreCase(other.nome) && tipo == other.tipo;
    }

    @Override
    public int hashCode()
    {
        return 31 * nome.toLowerCase().hashCode() + tipo.hashCode();
    }

    @Override
    public String toString()
    {
        return nome + " [" + tipo + ", " + tipoDato + "] ("
               + (obbligatorio ? "obbligatorio" : "facoltativo") + ")";
    }

    /** Utility: case-insensitive search by name in a list. */
    public static boolean containsNome(List<Campo> campi, String nome)
    {
        if (nome == null) return false;
        return campi.stream().anyMatch(c -> c.getNome().equalsIgnoreCase(nome.trim()));
    }
}
