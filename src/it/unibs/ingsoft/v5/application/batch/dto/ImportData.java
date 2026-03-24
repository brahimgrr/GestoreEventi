package it.unibs.ingsoft.v5.application.batch.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/**
 * Root DTO representing the entire content of a batch-import JSON file.
 * All sections are optional — an empty or partial file is valid.
 */
public final class ImportData {

    private final List<CampoImportDTO>     campiComuni;
    private final List<CategoriaImportDTO> categorie;
    private final List<PropostaImportDTO>  proposte;

    @JsonCreator
    public ImportData(
            @JsonProperty("campiComuni") List<CampoImportDTO>     campiComuni,
            @JsonProperty("categorie")   List<CategoriaImportDTO> categorie,
            @JsonProperty("proposte")    List<PropostaImportDTO>  proposte)
    {
        this.campiComuni = campiComuni != null ? campiComuni : Collections.emptyList();
        this.categorie   = categorie   != null ? categorie   : Collections.emptyList();
        this.proposte    = proposte    != null ? proposte    : Collections.emptyList();
    }

    public List<CampoImportDTO>     getCampiComuni() { return campiComuni; }
    public List<CategoriaImportDTO> getCategorie()   { return categorie; }
    public List<PropostaImportDTO>  getProposte()    { return proposte; }
}
