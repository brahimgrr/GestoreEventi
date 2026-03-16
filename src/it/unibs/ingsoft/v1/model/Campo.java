package it.unibs.ingsoft.v1.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public final class Campo implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    private final String    nome;
    private final TipoCampo tipo;
    private boolean         obbligatorio;

    public Campo(String nome, TipoCampo tipo, boolean obbligatorio)
    {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome campo non valido.");

        this.nome         = nome.trim();
        this.tipo         = Objects.requireNonNull(tipo, "TipoCampo nullo.");
        this.obbligatorio = obbligatorio;
    }

    // ---------------------------------------------------------------
    // GETTER / SETTER
    // ---------------------------------------------------------------

    public String getNome()
    {
        return nome;
    }

    public TipoCampo getTipo()
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

    // ---------------------------------------------------------------
    // EQUALS / HASHCODE / TOSTRING
    // ---------------------------------------------------------------

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (!(o instanceof Campo campo))
            return false;

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
        return nome + " [" + tipo + "]" + (obbligatorio ? " (obbligatorio)" : " (facoltativo)");
    }
}