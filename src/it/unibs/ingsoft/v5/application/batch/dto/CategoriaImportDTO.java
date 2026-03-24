package it.unibs.ingsoft.v5.application.batch.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/**
 * DTO for importing a category (with its specific fields) from a batch file.
 */
public final class CategoriaImportDTO {

    private final String                      nome;
    private final List<CampoSpecificoImportDTO> campiSpecifici;

    @JsonCreator
    public CategoriaImportDTO(
            @JsonProperty("nome")           String                        nome,
            @JsonProperty("campiSpecifici") List<CampoSpecificoImportDTO> campiSpecifici)
    {
        this.nome           = nome;
        this.campiSpecifici = campiSpecifici != null ? campiSpecifici : Collections.emptyList();
    }

    public String                        getNome()           { return nome; }
    public List<CampoSpecificoImportDTO> getCampiSpecifici() { return campiSpecifici; }
}
