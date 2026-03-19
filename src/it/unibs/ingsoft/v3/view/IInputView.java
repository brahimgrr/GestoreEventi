package it.unibs.ingsoft.v3.view;

import it.unibs.ingsoft.v3.model.TipoDato;
import java.util.List;

/**
 * ISP sub-interface: pure input / acquisition operations.
 */
public interface IInputView
{
    String acquisisciStringa(String prompt);

    /** @pre min <= max */
    int acquisisciIntero(String prompt, int min, int max);

    boolean acquisisciSiNo(String prompt);

    TipoDato acquisisciTipoDato(String prompt);
    List<String> acquisisciListaNomi(String titolo);
}
