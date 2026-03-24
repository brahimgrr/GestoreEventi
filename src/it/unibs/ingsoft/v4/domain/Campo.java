package it.unibs.ingsoft.v4.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Immutable value object representing a field definition (base, common, or category-specific).
 */
public final class Campo
{
    private final String    nome;
    private final TipoCampo tipo;
    private final TipoDato tipoDato;
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
                 @JsonProperty("tipo") TipoCampo tipo,
                 @JsonProperty("tipoDato") TipoDato tipoDato,
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

    public Campo(Campo oldCampo) {
        this.nome = oldCampo.nome;
        this.tipo = oldCampo.tipo;
        this.tipoDato = oldCampo.tipoDato;
        this.obbligatorio = oldCampo.obbligatorio;
    }

    public String getNome() {
        return nome;
    }

    public TipoCampo getTipo(){
        return tipo;
    }

    public TipoDato getTipoDato() {
        return tipoDato;
    }

    public boolean isObbligatorio() {
        return obbligatorio;
    }

    /**
     * Returns a new Campo identical to this one but with the given {@code obbligatorio} value.
     */
    public Campo withObbligatorio(boolean obbligatorio)
    {
        return new Campo(this.nome, this.tipo, this.tipoDato, obbligatorio);
    }

    /** Case-insensitive name equality. */
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
