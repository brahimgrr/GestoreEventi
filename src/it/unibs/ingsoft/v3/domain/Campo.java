package it.unibs.ingsoft.v3.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Immutable value object representing a field definition (base, common, or category-specific).
 *
 * <p>To change the {@code obbligatorio} flag, use {@link #withObbligatorio(boolean)},
 * which returns a new instance rather than mutating this one.</p>
 */
public final class Campo
{
    private final String    nome;
    private final TipoCampo tipo;
    private final TipoDato  tipoDato;
    private final boolean   obbligatorio;

    /** Primary constructor — also used as the Jackson deserialisation entry point. */
    @JsonCreator
    public Campo(@JsonProperty("nome")         String    nome,
                 @JsonProperty("tipo")         TipoCampo tipo,
                 @JsonProperty("tipoDato")     TipoDato  tipoDato,
                 @JsonProperty("obbligatorio") boolean   obbligatorio)
    {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome campo non valido.");

        this.nome         = nome.trim();
        this.tipo         = Objects.requireNonNull(tipo,     "TipoCampo nullo.");
        this.tipoDato     = Objects.requireNonNull(tipoDato, "TipoDato nullo.");
        this.obbligatorio = obbligatorio;
    }

    public String    getNome()        { return nome; }
    public TipoCampo getTipo()        { return tipo; }
    public TipoDato  getTipoDato()    { return tipoDato; }
    public boolean   isObbligatorio() { return obbligatorio; }

    /** Returns a new Campo identical to this one but with the given {@code obbligatorio} value. */
    public Campo withObbligatorio(boolean obbligatorio)
    {
        return new Campo(this.nome, this.tipo, this.tipoDato, obbligatorio);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Campo)) return false;
        Campo campo = (Campo) o;
        return nome.equalsIgnoreCase(campo.nome) && tipo == campo.tipo;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(nome.toLowerCase(), tipo);
    }

    @Override
    public String toString()
    {
        return nome
                + " [" + tipo + ", " + tipoDato + "]"
                + (obbligatorio ? " (obbligatorio)" : " (facoltativo)");
    }
}
