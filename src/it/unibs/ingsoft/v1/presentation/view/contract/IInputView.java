package it.unibs.ingsoft.v1.presentation.view.contract;

import it.unibs.ingsoft.v1.domain.TipoDato;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * ISP sub-interface: pure input / acquisition operations.
 * Implementations that only need to read user input (e.g., a scripted test driver)
 * depend only on this narrow interface.
 */
public interface IInputView {
    /**
     * Context hint shown at the start of every form that accepts free-text input.
     */
    String HINT_ANNULLA = "Digita 'annulla' per annullare.";

    /**
     * Reads a string from the user. Implementations must detect the cancel keyword
     * ("annulla") and throw OperationCancelledException when it is typed.
     */
    String acquisisciStringa(String prompt);

    /**
     * Prompts until the supplied predicate is satisfied, showing errorMsg on failure.
     */
    String acquisisciStringaConValidazione(String prompt, Predicate<String> validatore, String errorMsg);

    String acquisisciPassword(String prompt);

    int acquisisciIntero(String prompt, int min, int max);

    boolean acquisisciSiNo(String prompt);

    TipoDato acquisisciTipoDato(String prompt);

    /**
     * Interactively collects a list of names with inline duplicate detection
     * and a review/confirm step before returning.
     */
    List<String> acquisisciListaNomi(String titolo);

    /**
     * Presents a numbered list and returns the selected element,
     * or {@link Optional#empty()} if the user chooses 0 (Annulla).
     */
    <T> Optional<T> selezionaElemento(String prompt, List<T> elementi);

    /**
     * Like {@link #selezionaElemento} but appends extra info per element
     * (e.g., "[obbligatorio]" or "[facoltativo]").
     */
    <T> Optional<T> selezionaElementoConInfo(String prompt, List<T> elementi,
                                             Function<T, String> infoMapper);
}
