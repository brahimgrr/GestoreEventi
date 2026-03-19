package it.unibs.ingsoft.v1.presentation.view.contract;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * ISP sub-interface: pure input / acquisition operations.
 * Implementations that only need to read user input (e.g., a scripted test driver)
 * depend only on this narrow interface.
 */
public interface IInputView
{
    String acquisisciStringa(String prompt);

    /**
     * Acquires a string, re-prompting inline until {@code validator} passes.
     * Throws {@link it.unibs.ingsoft.v1.presentation.view.cli.ConsoleUI.CancelException}
     * if the user types the cancel keyword.
     *
     * @param messaggioErrore shown with ❌ on each failed validation attempt
     */
    String acquisisciStringaConValidazione(String prompt,
                                           Predicate<String> validator,
                                           String messaggioErrore);

    /**
     * Acquires a password, masking input via {@link java.io.Console#readPassword}
     * when available. Falls back to plain {@link #acquisisciStringa} otherwise.
     */
    String acquisisciPassword(String prompt);

    /**
     * @pre min &lt;= max
     */
    int acquisisciIntero(String prompt, int min, int max);

    boolean acquisisciSiNo(String prompt);

    /**
     * Interactively collects a list of names, one per line.
     * Detects duplicates inline (case-insensitive) and warns without discarding.
     * Shows a running count in the prompt and a review/confirm step before returning.
     * Blank line terminates entry.
     */
    List<String> acquisisciListaNomi(String titolo);

    /**
     * Presents a numbered list (1..N + 0 to abort) and returns the chosen element.
     * Returns {@code Optional.empty()} when the list is empty or the user chooses 0.
     */
    <T> Optional<T> selezionaElemento(String prompt, List<T> elementi);

    /**
     * Like {@link #selezionaElemento} but appends {@code infoMapper.apply(element)}
     * in square brackets after each element name — e.g. {@code "  1) Durata  [obbligatorio]"}.
     */
    <T> Optional<T> selezionaElementoConInfo(String prompt,
                                              List<T> elementi,
                                              Function<T, String> infoMapper);
}
