package it.unibs.ingsoft.v2.presentation.view.contract;

import it.unibs.ingsoft.v2.domain.Categoria;
import it.unibs.ingsoft.v2.domain.Proposta;
import it.unibs.ingsoft.v2.domain.TipoDato;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * ISP sub-interface: pure input / acquisition operations.
 * Implementations that only need to read user input (e.g., a scripted test driver)
 * depend only on this narrow interface.
 */
public interface IInputView {
    /**
     * Reads a string from the user. Implementations must detect the cancel keyword
     * ("annulla") and throw {@link OperationCancelledException} when it is typed.
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

    /**
     * Shows a numbered list of categories and returns the 0-based index of the
     * selected one, or empty if the user cancels.
     */
    OptionalInt selezionaCategoria(List<Categoria> categorie);

    /**
     * Runs the full proposal form and returns the inserted values, or empty if the user cancels.
     */
    Optional<Map<String, String>> acquisisciValoriProposta(Proposta proposta, ProposalFieldValidator validator);

    /**
     * Runs a correction form limited to the given field names and returns the edited values,
     * or empty if the user cancels.
     */
    Optional<Map<String, String>> correggiCampiProposta(Proposta proposta, Set<String> nomiCampi, ProposalFieldValidator validator);
}
