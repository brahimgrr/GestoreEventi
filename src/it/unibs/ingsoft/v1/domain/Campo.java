package it.unibs.ingsoft.v1.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * Immutable value object representing a field.
 * Identity is determined by name only (case-insensitive), as field names are globally unique.
 */
public final class Campo
{
    private final String    nome;
    private final TipoCampo tipo;
    private final boolean   obbligatorio;

    @JsonCreator
    public Campo(@JsonProperty("nome")         String    nome,
                 @JsonProperty("tipo")         TipoCampo tipo,
                 @JsonProperty("obbligatorio") boolean   obbligatorio)
    {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome campo non valido.");

        this.nome         = nome.trim();
        this.tipo         = Objects.requireNonNull(tipo, "Tipo nullo.");
        this.obbligatorio = obbligatorio;
    }

    public String getNome()        { return nome; }
    public TipoCampo getTipo()     { return tipo; }
    public boolean isObbligatorio(){ return obbligatorio; }

    /**
     * Returns a new {@code Campo} identical to this one except for the {@code obbligatorio} flag.
     * Use this instead of a setter to keep {@code Campo} immutable.
     */
    public Campo withObbligatorio(boolean nuovoValore)
    {
        return new Campo(this.nome, this.tipo, nuovoValore);
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
        return nome + (obbligatorio ? "  (obbligatorio)" : "");
    }
}
