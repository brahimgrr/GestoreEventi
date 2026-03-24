package it.unibs.ingsoft.v5.presentation.view.contract;

import it.unibs.ingsoft.v5.domain.Campo;
import it.unibs.ingsoft.v5.domain.Categoria;
import it.unibs.ingsoft.v5.domain.Proposta;

import java.util.List;
import java.util.Map;

/**
 * ISP sub-interface: pure output / display operations.
 * Implementations that only need to produce output (e.g., a logger, a test spy)
 * depend only on this narrow interface.
 *
 * <p>This interface exposes the domain objects directly because the current
 * CLI renders them without an additional presentation layer.</p>
 */
public interface IOutputView
{
    void stampa(String testo);
    void newLine();
    void header(String titolo);
    void stampaSezione(String titolo);
    void stampaCampi(List<Campo> campi);
    void stampaCategorie(List<Categoria> categorie);

    /** Displays categories with their specific fields listed below each. */
    void stampaCategorieDettaglio(Map<String, List<String>> categorieConCampi);

    /** Displays a numbered menu; {@code 0} exits/goes back with the label "Esci". */
    void stampaMenu(String titolo, String[] voci);

    /** Displays a numbered menu; {@code 0} exits/goes back with the custom {@code uscitaLabel}. */
    void stampaMenu(String titolo, String[] voci, String uscitaLabel);

    void pausa();

    void stampaSuccesso(String msg);
    void stampaErrore(String msg);
    void stampaAvviso(String msg);
    void stampaInfo(String msg);

    /** Displays the bacheca (bulletin board) organised by category. */
    void mostraBacheca(Map<String, List<Proposta>> bacheca);

    /** Displays a single-proposal summary box. */
    void mostraRiepilogoProposta(Proposta proposta);

    /** Prints a blank line then waits for ENTER — convenience for the common end-of-action pattern. */
    default void pausaConSpaziatura() {
        newLine();
        pausa();
    }
}
