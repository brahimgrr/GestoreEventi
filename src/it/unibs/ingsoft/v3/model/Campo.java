package it.unibs.ingsoft.v3.model;

import java.io.Serializable;
import java.util.Objects;

public final class Campo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String    nome;
    private final TipoCampo tipo;
    private final TipoDato tipoDato;
    private boolean         obbligatorio;

    public Campo(String nome, TipoCampo tipo, TipoDato tipoDato, boolean obbligatorio)
    {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome campo non valido.");

        this.nome = nome.trim();
        this.tipo = Objects.requireNonNull(tipo,     "TipoCampo nullo.");
        this.tipoDato = Objects.requireNonNull(tipoDato, "TipoDato nullo.");
        this.obbligatorio = obbligatorio;
    }

    public String    getNome()
    {
        return nome;
    }

    public TipoCampo getTipo()
    {
        return tipo;
    }

    public TipoDato getTipoDato()
    {
        return tipoDato;
    }

    public boolean   isObbligatorio()
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