package it.unibs.ingsoft.v3.application;

import it.unibs.ingsoft.v3.domain.*;
import it.unibs.ingsoft.v3.persistence.dto.CatalogoData;
import it.unibs.ingsoft.v3.persistence.api.ICategoriaRepository;

import java.util.*;

/**
 * Service responsible for base and common field management.
 * Split from the original {@code CategoriaService} to satisfy SRP.
 */
public final class CampoService
{
    private final ICategoriaRepository repo;
    private final CatalogoData         catalogo;

    /**
     * @pre repo     != null
     * @pre catalogo != null
     */
    public CampoService(ICategoriaRepository repo, CatalogoData catalogo)
    {
        this.repo     = Objects.requireNonNull(repo);
        this.catalogo = Objects.requireNonNull(catalogo);
        inizializzaCampiBaseFissi();
    }

    private void inizializzaCampiBaseFissi()
    {
        boolean modificato = false;

        for (CampoBaseDefinito cbd : CampoBaseDefinito.values())
        {
            boolean giaPresente = catalogo.getCampiBase().stream()
                    .anyMatch(c -> c.getNome().equalsIgnoreCase(cbd.getNomeCampo()));

            if (!giaPresente)
            {
                catalogo.addCampoBase(new Campo(cbd.getNomeCampo(), TipoCampo.BASE, cbd.getTipoDato(), true));
                modificato = true;
            }
        }

        if (modificato)
            repo.save(catalogo);
    }

    public void aggiungiCampiBaseExtra(List<String> nomi, List<TipoDato> tipi)
    {
        if (catalogo.isCampiBaseFissati())
            throw new IllegalStateException("I campi base extra sono già stati fissati e sono immutabili.");

        Objects.requireNonNull(nomi);
        Objects.requireNonNull(tipi);

        for (int i = 0; i < nomi.size(); i++)
        {
            String nome = normalizza(nomi.get(i));
            TipoDato td = tipi.get(i);

            if (CampoBaseDefinito.isNomeFisso(nome))
                throw new IllegalArgumentException("\"" + nome + "\" è un campo base fisso; non può essere aggiunto come extra.");

            if (nomeEsistente(nome))
                throw new IllegalArgumentException("Esiste già un campo con il nome: \"" + nome + "\".");

            catalogo.addCampoBase(new Campo(nome, TipoCampo.BASE, td, true));
        }

        catalogo.setCampiBaseFissati(true);
        repo.save(catalogo);
    }

    public void fissaCampiBaseSenzaExtra()
    {
        if (!catalogo.isCampiBaseFissati())
        {
            catalogo.setCampiBaseFissati(true);
            repo.save(catalogo);
        }
    }

    public boolean isCampiBaseFissati()
    {
        return catalogo.isCampiBaseFissati();
    }

    public List<Campo> getCampiBase()
    {
        return catalogo.getCampiBase();
    }

    public void addCampoComune(String nome, TipoDato tipoDato, boolean obbligatorio)
    {
        nome = normalizza(nome);

        if (nomeEsistente(nome))
            throw new IllegalArgumentException("Esiste già un campo con il nome: \"" + nome + "\".");

        catalogo.addCampoComune(new Campo(nome, TipoCampo.COMUNE, tipoDato, obbligatorio));
        repo.save(catalogo);
    }

    public boolean removeCampoComune(String nome)
    {
        final String n = normalizza(nome);
        boolean removed = catalogo.removeCampoComune(n);
        if (removed) repo.save(catalogo);
        return removed;
    }

    public boolean setObbligatorietaCampoComune(String nome, boolean obbligatorio)
    {
        final String n = normalizza(nome);
        Campo vecchio = catalogo.getCampiComuni().stream()
                .filter(c -> c.getNome().equalsIgnoreCase(n))
                .findFirst()
                .orElse(null);

        if (vecchio == null)
            return false;

        catalogo.replaceCampoComune(n, vecchio.withObbligatorio(obbligatorio));
        repo.save(catalogo);
        return true;
    }

    public List<Campo> getCampiComuni()
    {
        return catalogo.getCampiComuni();
    }

    boolean nomeBaseOComuneEsistente(String nome)
    {
        for (Campo c : catalogo.getCampiBase())
            if (c.getNome().equalsIgnoreCase(nome)) return true;
        for (Campo c : catalogo.getCampiComuni())
            if (c.getNome().equalsIgnoreCase(nome)) return true;
        return false;
    }

    private boolean nomeEsistente(String nome)
    {
        if (nomeBaseOComuneEsistente(nome)) return true;

        for (Categoria cat : catalogo.getCategorie())
            for (Campo c : cat.getCampiSpecifici())
                if (c.getNome().equalsIgnoreCase(nome)) return true;

        return false;
    }

    private String normalizza(String s)
    {
        if (s == null) throw new IllegalArgumentException("Nome non valido (null).");
        return s.trim();
    }
}
