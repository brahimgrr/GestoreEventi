package it.unibs.ingsoft.v1.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * Immutable value object representing a field.
 *
 * <p><b>Invariant:</b> {@code nome} is never null or blank; {@code tipo} and {@code tipoDato} are never null.</p>
 * <p>Identity is determined by name only (case-insensitive), as field names must be unique
 * within their scope (globally for base/common, per-category for specific fields).</p>
 */
public final class Campo
{
    private final String    nome;
    private final TipoCampo tipo;
    private final TipoDato  tipoDato;
    private final boolean   obbligatorio;

    /**
     * @pre  nome != null &amp;&amp; !nome.isBlank()
     * @pre  tipo != null
     * @pre  tipoDato != null
     * @post getNome().equals(nome.trim())
     * @post getTipo() == tipo
     * @post getTipoDato() == tipoDato
     * @post isObbligatorio() == obbligatorio
     */
    @JsonCreator
    public Campo(@JsonProperty("nome")         String    nome,
                 @JsonProperty("tipo")         TipoCampo tipo,
                 @JsonProperty("tipoDato")     TipoDato  tipoDato,
                 @JsonProperty("obbligatorio") boolean   obbligatorio)
    {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome campo non valido.");

        this.nome         = nome.trim();
        this.tipo         = Objects.requireNonNull(tipo, "Tipo nullo.");
        this.tipoDato     = tipoDato != null ? tipoDato : TipoDato.STRINGA;
        this.obbligatorio = obbligatorio;
    }

    /**
     * Convenience constructor that defaults {@code tipoDato} to {@link TipoDato#STRINGA}.
     */
    public Campo(String nome, TipoCampo tipo, boolean obbligatorio)
    {
        this(nome, tipo, TipoDato.STRINGA, obbligatorio);
    }

    public String getNome()         { return nome; }
    public TipoCampo getTipo()      { return tipo; }
    public TipoDato getTipoDato()   { return tipoDato; }
    public boolean isObbligatorio() { return obbligatorio; }

    /**
     * Returns a new {@code Campo} identical to this one except for the {@code obbligatorio} flag.
     * Use this instead of a setter to keep {@code Campo} immutable.
     */
    public Campo withObbligatorio(boolean nuovoValore)
    {
        return new Campo(this.nome, this.tipo, this.tipoDato, nuovoValore);
    }

    // ---------------------------------------------------------------
    // Static utility
    // ---------------------------------------------------------------

    /**
     * Returns {@code true} if any field in {@code campi} has the given name (case-insensitive).
     */
    public static boolean containsNome(List<Campo> campi, String nome)
    {
        return campi.stream().anyMatch(c -> c.getNome().equalsIgnoreCase(nome));
    }

    // ---------------------------------------------------------------
    // Identity: name only (case-insensitive) — type is irrelevant for uniqueness
    // ---------------------------------------------------------------

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Campo)) return false;
        return nome.equalsIgnoreCase(((Campo) o).nome);
    }

    @Override
    public int hashCode()
    {
        return nome.toLowerCase().hashCode();
    }

    @Override
    public String toString()
    {
        return nome + " [" + tipoDato + "]" + (obbligatorio ? "  (obbligatorio)" : "");
    }
}
