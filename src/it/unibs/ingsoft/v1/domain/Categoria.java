package it.unibs.ingsoft.v1.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * Rich domain object for a category.
 * Enforces its own invariants: specific fields must have type SPECIFICO and names must
 * be unique within this category. Cross-category uniqueness is the service's concern.
 */
public final class Categoria
{
    private final String      nome;
    private final List<Campo> campiSpecifici = new ArrayList<>();

    public Categoria(String nome)
    {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome categoria non valido.");
        this.nome = nome.trim();
    }

    /** Jackson deserialization factory — restores both name and specific fields. */
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

    public String getNome() { return nome; }

    public List<Campo> getCampiSpecifici()
    {
        return Collections.unmodifiableList(campiSpecifici);
    }

    // ---------------------------------------------------------------
    // Domain invariant enforcement
    // ---------------------------------------------------------------

    public void addCampoSpecifico(Campo campoSpecifico)
    {
        Objects.requireNonNull(campoSpecifico, "Campo nullo.");

        if (campoSpecifico.getTipo() != TipoCampo.SPECIFICO)
            throw new IllegalArgumentException("Il campo deve avere scope SPECIFICO.");

        if (Campo.containsNome(campiSpecifici, campoSpecifico.getNome()))
            throw new IllegalArgumentException(
                "Esiste già un campo specifico con questo nome nella categoria.");

        campiSpecifici.add(campoSpecifico);
    }

    public boolean removeCampoSpecifico(String nomeCampo)
    {
        return campiSpecifici.removeIf(c -> c.getNome().equalsIgnoreCase(nomeCampo));
    }

    /**
     * Replaces the specific field with the updated {@code obbligatorio} value.
     * {@code Campo} is immutable, so the element is replaced rather than mutated.
     *
     * @return {@code true} if the field was found and replaced, {@code false} otherwise
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

    // ---------------------------------------------------------------
    // equals / hashCode / toString — identity is name only
    // ---------------------------------------------------------------

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Categoria)) return false;
        return nome.equalsIgnoreCase(((Categoria) o).nome);
    }

    @Override
    public int hashCode()
    {
        return nome.toLowerCase().hashCode();
    }

    @Override
    public String toString()
    {
        return nome;
    }
}
