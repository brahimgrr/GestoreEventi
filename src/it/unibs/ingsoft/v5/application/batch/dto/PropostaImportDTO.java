package it.unibs.ingsoft.v5.application.batch.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Map;

/**
 * DTO for importing a proposal from a batch file.
 * The {@code categoria} field is the name of an existing category in the catalogue.
 */
public final class PropostaImportDTO {

    private final String              categoria;
    private final Map<String, String> valoriCampi;

    @JsonCreator
    public PropostaImportDTO(
            @JsonProperty("categoria")   String              categoria,
            @JsonProperty("valoriCampi") Map<String, String> valoriCampi)
    {
        this.categoria   = categoria;
        this.valoriCampi = valoriCampi != null ? valoriCampi : Collections.emptyMap();
    }

    public String              getCategoria()   { return categoria; }
    public Map<String, String> getValoriCampi() { return valoriCampi; }
}
