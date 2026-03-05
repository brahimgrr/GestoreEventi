import java.io.Serializable;
import java.util.*;

public final class Categoria implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final String nome; //DEV'ESSERE UNICO
    private final List<Campo> campiSpecifici = new ArrayList<>();

    public Categoria(String nome)
    {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome categoria non valido.");
        this.nome = nome.trim();
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

        if (campoSpecifico.getScope() != TipoCampo.SPECIFICO)
            throw new IllegalArgumentException("Il campo deve avere scope SPECIFICO.");

        if (containsCampo(campoSpecifico.getNome()))
            throw new IllegalArgumentException("Esiste già un campo specifico con questo nome nella categoria.");

        campiSpecifici.add(campoSpecifico);
        //campiSpecifici.sort(Comparator.comparing(c -> c.getNome().toLowerCase()));
    }

    public boolean removeCampoSpecifico(String nomeCampo)
    {
        return campiSpecifici.removeIf(c -> c.getNome().equalsIgnoreCase(nomeCampo));
    }

    public boolean setObbligatorietaCampoSpecifico(String nomeCampo, boolean obbligatorio)
    {
        for (Campo c : campiSpecifici)
        {
            if (c.getNome().equalsIgnoreCase(nomeCampo))
            {
                c.setObbligatorio(obbligatorio);
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
        return "Categoria{" +
                "nome='" + nome + '\'' +
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