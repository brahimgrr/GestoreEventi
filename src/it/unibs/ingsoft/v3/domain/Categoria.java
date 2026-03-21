package it.unibs.ingsoft.v3.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public final class Categoria
{
    private final String nome; // UNICO
    private final List<Campo> campiSpecifici = new ArrayList<>();

    public Categoria(String nome)
    {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome categoria non valido.");

        this.nome = nome.trim();
    }

    /** Jackson deserialisation factory — restores the name and its specific fields. */
    @JsonCreator
    public static Categoria fromJson(
            @JsonProperty("nome")           String      nome,
            @JsonProperty("campiSpecifici") List<Campo> campiSpecifici)
    {
        Categoria cat = new Categoria(nome);
        if (campiSpecifici != null)
            cat.campiSpecifici.addAll(campiSpecifici);
        return cat;
    }

    public String getNome()
    {
        return nome;
    }

    public List<Campo> getCampiSpecifici()
    {
        return Collections.unmodifiableList(campiSpecifici);
    }

    public void addCampoSpecifico(Campo campoSpecifico)
    {
        Objects.requireNonNull(campoSpecifico, "Campo nullo.");

        if (campoSpecifico.getTipo() != TipoCampo.SPECIFICO)
            throw new IllegalArgumentException("Il campo dev'essere di tipo SPECIFICO.");

        if (containsCampo(campoSpecifico.getNome()))
            throw new IllegalArgumentException("Esiste già un campo specifico con questo nome nella categoria.");

        campiSpecifici.add(campoSpecifico);
        campiSpecifici.sort(Comparator.comparing(c -> c.getNome().toLowerCase()));
    }

    public boolean removeCampoSpecifico(String nomeCampo)
    {
        return campiSpecifici.removeIf(c -> c.getNome().equalsIgnoreCase(nomeCampo));
    }

    /**
     * Updates the mandatory flag of the named specific field.
     * Replaces the existing (immutable) {@link Campo} with a new instance via
     * {@link Campo#withObbligatorio(boolean)}.
     *
     * @return true if the field was found and updated, false otherwise
     */
    public boolean setObbligatorietaCampoSpecifico(String nomeCampo, boolean obbligatorio)
    {
        for (int i = 0; i < campiSpecifici.size(); i++)
        {
            if (campiSpecifici.get(i).getNome().equalsIgnoreCase(nomeCampo))
            {
                campiSpecifici.set(i, campiSpecifici.get(i).withObbligatorio(obbligatorio));
                return true;
            }
        }

        return false;
    }

    public boolean containsCampo(String nomeCampo)
    {
        for (Campo c : campiSpecifici)
        {
            if (c.getNome().equalsIgnoreCase(nomeCampo))
                return true;
        }

        return false;
    }

    @Override
    public String toString()
    {
        return  "nome='" + nome + '\'' +
                ", campiSpecifici=" + campiSpecifici +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (!(o instanceof Categoria))
            return false;

        Categoria categoria = (Categoria) o;

        return nome.equalsIgnoreCase(categoria.nome);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(nome.toLowerCase());
    }
}
