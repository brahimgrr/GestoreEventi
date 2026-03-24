package it.unibs.ingsoft.v5.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Represents an event category. Each category has a unique name and a list of
 * category-specific fields (sorted alphabetically by name).
 */
public final class Categoria
{
    private final String     nome;
    private final List<Campo> campiSpecifici;

    public Categoria(String nome)
    {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Il nome della categoria non può essere vuoto.");
        this.nome           = nome.trim();
        this.campiSpecifici = new ArrayList<>();
    }

    public Categoria(Categoria oldCategoria) {
        this.nome = oldCategoria.nome;
        this.campiSpecifici =  oldCategoria.campiSpecifici.stream().map(Campo::new).toList();
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

    public String getNome() { return nome; }

    public List<Campo> getCampiSpecifici()
    {
        return Collections.unmodifiableList(campiSpecifici);
    }

    /**
     * Adds a specific field to this category.
     *
     * @pre c.getTipo() == TipoCampo.SPECIFICO
     * @pre no existing specific field has the same name (case-insensitive)
     * @throws IllegalArgumentException if the field type is wrong or name is duplicate
     */
    public void addCampoSpecifico(Campo campoSpecifico)
    {
        if (campoSpecifico.getTipo() != TipoCampo.SPECIFICO)
            throw new IllegalArgumentException("Solo campi di tipo SPECIFICO possono essere aggiunti a una categoria.");
        if (containsCampo(campoSpecifico.getNome()))
            throw new IllegalArgumentException(
                    "La categoria \"" + nome + "\" ha già un campo chiamato \"" + campoSpecifico.getNome() + "\".");

        campiSpecifici.add(campoSpecifico);
        campiSpecifici.sort(Comparator.comparing(Campo::getNome, String.CASE_INSENSITIVE_ORDER));
    }

    /**
     * Removes the specific field with the given name (case-insensitive).
     *
     * @return true if removed, false if not found
     */
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

    private boolean containsCampo(String nomeCampo)
    {
        return campiSpecifici.stream().anyMatch(c -> c.getNome().equalsIgnoreCase(nomeCampo));
    }

    /** Case-insensitive name equality. */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof Categoria)) return false;
        return nome.equalsIgnoreCase(((Categoria) obj).nome);
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
