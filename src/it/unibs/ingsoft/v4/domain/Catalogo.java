package it.unibs.ingsoft.v4.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * Pure serializable DTO for the field/category catalogue.
 * Contains no domain query logic — all business operations live in the service layer.
 * Structural mutations (add/remove/replace) are deliberately kept here so the
 * service does not need to rebuild the full list on every change.
 */
public final class Catalogo {
    private final List<Campo> campiBase = new ArrayList<>();
    private final List<Campo> campiComuni = new ArrayList<>();
    private final List<Categoria> categorie = new ArrayList<>();

    private boolean campiBaseFissati;

    /**
     * Jackson deserialization factory.
     */
    @JsonCreator
    public static Catalogo fromJson(
            @JsonProperty("campiBase") List<Campo> campiBase,
            @JsonProperty("campiBaseFissati") boolean campiBaseFissati,
            @JsonProperty("campiComuni") List<Campo> campiComuni,
            @JsonProperty("categorie") List<Categoria> categorie) {
        Catalogo d = new Catalogo();
        if (campiBase != null) d.campiBase.addAll(campiBase);
        if (campiBaseFissati) d.campiBaseFissati = true;
        if (campiComuni != null) d.campiComuni.addAll(campiComuni);
        if (categorie != null) d.categorie.addAll(categorie);
        return d;
    }
    // ---------------- CAMPI BASE ----------------

    public void fissareCampiBase(List<Campo> base, List<Campo> extra) {
        if (campiBaseFissati)
            throw new IllegalStateException("Campi base già fissati.");

        campiBase.clear();

        Set<String> nomi = new HashSet<>();

        for (Campo c : base) {
            addNomeUnico(nomi, c.getNome());
            campiBase.add(c);
        }

        if (extra != null) {
            for (Campo c : extra) {
                if (c == null || c.getNome().isBlank()) continue;

                addNomeUnico(nomi, c.getNome());

                if (nomeEsistenteGlobale(c.getNome()))
                    throw new IllegalArgumentException("Campo già esistente: " + c.getNome());

                campiBase.add(c);
            }
        }

        campiBaseFissati = true;
    }

    private void addNomeUnico(Set<String> set, String nome) {
        if (!set.add(nome.toLowerCase()))
            throw new IllegalArgumentException("Duplicato: " + nome);
    }

    public boolean isCampiBaseFissati() {
        return campiBaseFissati;
    }

    // ---------------- CAMPI COMUNI ----------------

    public void addCampoComune(Campo campo) {
        if (nomeEsistenteGlobale(campo.getNome()))
            throw new IllegalArgumentException("Campo già esistente.");

        campiComuni.add(campo);
    }

    public boolean removeCampoComune(String nome) {
        return campiComuni.removeIf(c -> c.getNome().equalsIgnoreCase(nome));
    }

    public boolean updateCampoComune(String nome, boolean obbligatorio) {
        for (int i = 0; i < campiComuni.size(); i++) {
            Campo c = campiComuni.get(i);
            if (c.getNome().equalsIgnoreCase(nome)) {
                campiComuni.set(i, c.withObbligatorio(obbligatorio));
                return true;
            }
        }
        return false;
    }

    // ---------------- CATEGORIE ----------------

    public Categoria addCategoria(String nome) {
        if (findCategoria(nome).isPresent())
            throw new IllegalArgumentException("Categoria già esistente.");

        Categoria c = new Categoria(nome);
        categorie.add(c);
        return c;
    }

    public boolean removeCategoria(String nome) {
        return categorie.removeIf(c -> c.getNome().equalsIgnoreCase(nome));
    }

    public Categoria getCategoriaOrThrow(String nome) {
        return findCategoria(nome)
                .orElseThrow(() -> new IllegalArgumentException("Categoria non trovata."));
    }

    private Optional<Categoria> findCategoria(String nome) {
        return categorie.stream()
                .filter(c -> c.getNome().equalsIgnoreCase(nome))
                .findFirst();
    }

    // ---------------- CAMPI SPECIFICI ----------------

    public void addCampoSpecifico(String categoria, Campo campo) {
        if (nomeEsistenteGlobale(campo.getNome()))
            throw new IllegalArgumentException("Campo già esistente.");

        getCategoriaOrThrow(categoria).addCampoSpecifico(campo);
    }

    public boolean removeCampoSpecifico(String categoria, String nome) {
        return getCategoriaOrThrow(categoria).removeCampoSpecifico(nome);
    }

    public boolean updateCampoSpecifico(String categoria, String nome, boolean obbligatorio) {
        return getCategoriaOrThrow(categoria)
                .setObbligatorietaCampoSpecifico(nome, obbligatorio);
    }

    // ---------------- QUERY ----------------

    public List<Campo> getCampiBase() {
        return List.copyOf(campiBase);
    }

    public List<Campo> getCampiComuni() {
        return List.copyOf(campiComuni);
    }

    public List<Categoria> getCategorie() {
        return List.copyOf(categorie);
    }

    public boolean nomeEsistenteGlobale(String nome) {
        return campiBase.stream().anyMatch(c -> c.getNome().equalsIgnoreCase(nome)) ||
                campiComuni.stream().anyMatch(c -> c.getNome().equalsIgnoreCase(nome));
    }
}
