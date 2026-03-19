package it.unibs.ingsoft.v2.presentation.view.cli.viewmodel;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * View model for displaying a proposal.
 * V2 has no enrollment concept, so there is no {@code numeroIscritti} field.
 */
public record PropostaVM(
        String categoriaNome,
        String stato,
        LocalDate dataPubblicazione,
        LocalDate termineIscrizione,
        List<String> campiOrdinati,
        Map<String, String> valoriCampi) {}
