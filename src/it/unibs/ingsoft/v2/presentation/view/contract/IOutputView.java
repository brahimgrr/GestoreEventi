package it.unibs.ingsoft.v2.presentation.view.contract;

import it.unibs.ingsoft.v2.domain.Campo;
import it.unibs.ingsoft.v2.domain.Categoria;
import it.unibs.ingsoft.v2.presentation.view.cli.viewmodel.PropostaVM;

import java.util.List;
import java.util.Map;

public interface IOutputView
{
    void stampa(String testo);
    void newLine();
    void header(String titolo);
    void stampaSezione(String titolo);
    void stampaCampi(List<Campo> campi);
    void stampaCategorie(List<Categoria> categorie);
    void stampaMenu(String titolo, String[] voci);
    void pausa();

    void stampaSuccesso(String msg);
    void stampaErrore(String msg);
    void stampaAvviso(String msg);
    void stampaInfo(String msg);

    /** Displays the bacheca (bulletin board) organised by category. */
    void mostraBacheca(Map<String, List<PropostaVM>> bacheca);

    /** Displays a single-proposal summary box. */
    void mostraRiepilogoProposta(PropostaVM proposta);

    default void pausaConSpaziatura()
    {
        newLine();
        pausa();
    }
}
