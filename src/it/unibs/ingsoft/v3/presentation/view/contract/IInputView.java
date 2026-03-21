package it.unibs.ingsoft.v3.presentation.view.contract;

import it.unibs.ingsoft.v3.domain.TipoDato;
import java.util.List;

/**
 * ISP sub-interface: pure input / acquisition operations.
 */
public interface IInputView
{
    String acquisisciStringa(String prompt);

    /**
     * Acquires a password without echoing to stdout if a console is available.
     * Falls back to {@link #acquisisciStringa} when running without a real console (e.g. in IDE).
     */
    String acquisisciPassword(String prompt);

    /** @pre min <= max */
    int acquisisciIntero(String prompt, int min, int max);

    boolean acquisisciSiNo(String prompt);

    TipoDato acquisisciTipoDato(String prompt);
    List<String> acquisisciListaNomi(String titolo);
}
