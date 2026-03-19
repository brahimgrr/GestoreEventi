package it.unibs.ingsoft.v4.view;

import it.unibs.ingsoft.v4.model.TipoDato;

import java.util.List;

/**
 * Pure input operations: acquiring data from the user.
 */
public interface IInputView
{
    String acquisisciStringa(String prompt);
    int acquisisciIntero(String prompt, int min, int max);
    boolean acquisisciSiNo(String prompt);
    TipoDato acquisisciTipoDato(String prompt);
    List<String> acquisisciListaNomi(String titolo);
}
