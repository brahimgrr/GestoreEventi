package it.unibs.ingsoft.v3.presentation.view.contract;

import java.util.List;

/**
 * ISP sub-interface: pure output / display operations.
 */
public interface IOutputView
{
    void stampa(String msg);
    void newLine();
    void header(String title);
    void stampaSezione(String titolo);
    void stampaCampi(List<?> campi);
    void stampaCategorie(List<?> cat);
    void stampaMenu(String titolo, String[] lista);
    void pausa();

    /** Messaggio di successo: "  ✅ msg" */
    void stampaSuccesso(String msg);
    /** Messaggio di errore:   "  ❌ msg" */
    void stampaErrore(String msg);
    /** Messaggio di avviso:   "  ⚠️  msg" */
    void stampaAvviso(String msg);
    /** Messaggio informativo: "  ℹ️  msg" */
    void stampaInfo(String msg);
}
