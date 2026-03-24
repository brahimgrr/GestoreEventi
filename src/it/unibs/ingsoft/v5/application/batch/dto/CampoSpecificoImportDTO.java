package it.unibs.ingsoft.v5.application.batch.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for importing a category-specific field from a batch file.
 */
public final class CampoSpecificoImportDTO {

    private final String  nome;
    private final String  tipoDato;
    private final boolean obbligatorio;

    @JsonCreator
    public CampoSpecificoImportDTO(
            @JsonProperty("nome")         String  nome,
            @JsonProperty("tipoDato")     String  tipoDato,
            @JsonProperty("obbligatorio") boolean obbligatorio)
    {
        this.nome         = nome;
        this.tipoDato     = tipoDato;
        this.obbligatorio = obbligatorio;
    }

    public String  getNome()         { return nome; }
    public String  getTipoDato()     { return tipoDato; }
    public boolean isObbligatorio()  { return obbligatorio; }
}
