package it.unibs.ingsoft.v2.view;

import it.unibs.ingsoft.v2.model.Campo;
import it.unibs.ingsoft.v2.model.Categoria;
import it.unibs.ingsoft.v2.model.Proposta;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * ISP sub-interface: domain-specific / composite UI operations.
 * Components that only need compound form rendering or domain display
 * depend only on this narrow interface.
 */
public interface ICompositeView
{
    Map<String, String> compilaCampiProposta(Map<String, String> valoriEsistenti,
            List<Campo> campiBase, List<Campo> campiComuni, List<Campo> campiSpecifici);

    void mostraBacheca(Map<String, List<Proposta>> bacheca,
            Function<Proposta, List<Campo>> campiProvider);

    /** Mostra il riepilogo formattato di una proposta prima della pubblicazione. */
    void mostraRiepilogoProposta(Proposta proposta, List<Campo> tuttiCampi);

    /** Ri-acquisisce solo i campi con errori (correzione parziale). */
    void correggiCampiNonValidi(Map<String, String> valori, List<Campo> campiConErrore);

    /** Mostra la lista numerata di categorie per la selezione. */
    void stampaCategorieSelezione(List<Categoria> categorie);
}
