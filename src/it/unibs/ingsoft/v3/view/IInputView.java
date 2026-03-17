package it.unibs.ingsoft.v3.view;

import it.unibs.ingsoft.v3.model.TipoDato;
import java.time.LocalDate;
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
    LocalDate acquisisciData(String prompt);

    /**
     * Acquisisce una data con vincolo: la data inserita deve essere SUCCESSIVA
     * di almeno 2 giorni a {@code minData}.
     * Mostra un errore inline se il vincolo non è rispettato.
     * Lancia {@link ConsoleUI.CancelException} se l'utente digita 'annulla'.
     *
     * @param minData  data minima di riferimento (può essere null → nessun vincolo)
     * @param nomeMin  nome del campo di riferimento, usato nel messaggio di errore
     */
    LocalDate acquisisciDataConVincolo(String prompt, LocalDate minData, String nomeMin);

    TipoDato acquisisciTipoDato(String prompt);
    List<String> acquisisciListaNomi(String titolo);
}
