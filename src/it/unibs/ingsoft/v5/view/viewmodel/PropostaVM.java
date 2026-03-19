package it.unibs.ingsoft.v5.view.viewmodel;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * View-model for displaying a Proposta in the bacheca, archivio,
 * or riepilogo views.
 *
 * @param campiOrdinati ordered list of campo names for display
 * @param valoriCampi   campo name → raw string value
 */
public record PropostaVM(
        String              categoriaNome,
        String              stato,
        LocalDate           dataPubblicazione,
        LocalDate           termineIscrizione,
        int                 numeroIscritti,
        List<String>        campiOrdinati,
        Map<String, String> valoriCampi
) {}
