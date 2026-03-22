package it.unibs.ingsoft.v2.presentation.view.viewmodel;

import java.util.List;
import java.util.Map;

/**
 * View model for displaying a proposal.
 * Dates are pre-formatted strings (DD/MM/YYYY) by {@link ViewModelMapper}.
 * V2 has no enrollment concept, so there is no {@code numeroIscritti} field.
 */
public record PropostaVM(
        String categoriaNome,
        String stato,
        Map<String, String> valoriCampi) {}
