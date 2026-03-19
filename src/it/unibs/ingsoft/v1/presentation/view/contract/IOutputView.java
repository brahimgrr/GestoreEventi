package it.unibs.ingsoft.v1.presentation.view.contract;

import it.unibs.ingsoft.v1.domain.Categoria;

import java.util.List;

/**
 * ISP sub-interface: pure output / display operations.
 * Implementations that only need to produce output (e.g., a logger, a test spy)
 * depend only on this narrow interface.
 */
public interface IOutputView
{
    void stampa(String msg);
    void newLine();
    void header(String title);
    void stampaSezione(String titolo);
    void stampaCampi(List<?> campi);
    void stampaCategorie(List<?> cat);

    /** Shows each category with its specific fields listed underneath. */
    void stampaCategorieDettaglio(List<Categoria> categorie);

    /**
     * Renders a numbered action menu.
     *
     * @param uscitaLabel label shown for option 0 (e.g., {@code "Esci"} or {@code "Torna"})
     */
    void stampaMenu(String titolo, String[] lista, String uscitaLabel);

    /**
     * Renders a numbered action menu with {@code "Esci"} as the option-0 label.
     * Use for the top-level main menu only; inner menus should use
     * {@link #stampaMenu(String, String[], String)} with {@code "Torna"}.
     */
    default void stampaMenu(String titolo, String[] lista) { stampaMenu(titolo, lista, "Esci"); }

    void pausa();

    /** Messaggio di successo: "  ✅ msg" */
    void stampaSuccesso(String msg);
    /** Messaggio di errore:   "  ❌ msg" */
    void stampaErrore(String msg);
    /** Messaggio di avviso:   "  ⚠️  msg" */
    void stampaAvviso(String msg);
    /** Messaggio informativo: "  ℹ️  msg" */
    void stampaInfo(String msg);

    /** Prints a blank line then waits for ENTER — convenience for the common end-of-action pattern. */
    default void pausaConSpaziatura() { newLine(); pausa(); }
}
