package it.unibs.ingsoft.v4.presentation.view.contract;

import it.unibs.ingsoft.v4.domain.Proposta;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface ProposalFieldValidator {
    List<String> validate(Proposta proposta, Map<String, String> valoriCorrenti, String nomeCampo, String valore);
}
