package it.unibs.ingsoft.v4.view;

import it.unibs.ingsoft.v4.model.Campo;
import it.unibs.ingsoft.v4.model.Categoria;
import it.unibs.ingsoft.v4.model.Notifica;
import it.unibs.ingsoft.v4.model.Proposta;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Compound / domain-specific view operations used by controllers.
 */
public interface ICompositeView
{
    Map<String, String> compilaCampiProposta(Map<String, String> valoriEsistenti,
            List<Campo> campiBase, List<Campo> campiComuni, List<Campo> campiSpecifici);

    void mostraBacheca(Map<String, List<Proposta>> bacheca,
            Function<Proposta, List<Campo>> campiProvider);

    void mostraArchivio(List<Proposta> archivio,
            Function<Proposta, List<Campo>> campiProvider);

    void mostraNotifiche(List<Notifica> notifiche);

    /** Mostra il riepilogo formattato di una proposta prima della pubblicazione. */
    void mostraRiepilogoProposta(Proposta proposta, List<Campo> tuttiCampi);

    /** Ri-acquisisce solo i campi con errori (correzione parziale). */
    void correggiCampiNonValidi(Map<String, String> valori, List<Campo> campiConErrore);

    /** Mostra le proposte aperte con dettaglio (data, luogo, quota) per la scelta iscrizione. */
    void mostraPropostePerIscrizione(List<Proposta> proposte);

    /** Mostra la lista numerata di categorie per la selezione. */
    void stampaCategorieSelezione(List<Categoria> categorie);

    /** Mostra la lista di proposte ritirabili con stato e data evento. */
    void stampaProposteRitirabili(List<Proposta> proposte);

    /** Mostra la lista di proposte per la disdetta con termine iscrizione. */
    void stampaProposteDisdici(List<Proposta> proposte);
}
