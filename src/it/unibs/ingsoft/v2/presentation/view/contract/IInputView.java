package it.unibs.ingsoft.v2.presentation.view.contract;

import it.unibs.ingsoft.v2.domain.TipoDato;

import java.util.List;
import java.util.function.Predicate;

public interface IInputView
{
    String acquisisciStringa(String prompt);

    /** Prompts until the supplied predicate is satisfied, showing errorMsg on failure. */
    String acquisisciStringaConValidazione(String prompt, Predicate<String> validatore, String errorMsg);

    String acquisisciPassword(String prompt);
    int    acquisisciIntero(String prompt, int min, int max);
    boolean acquisisciSiNo(String prompt);
    TipoDato acquisisciTipoDato(String prompt);
    List<String> acquisisciListaNomi(String prompt);
}
