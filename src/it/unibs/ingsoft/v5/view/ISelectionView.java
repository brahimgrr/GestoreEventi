package it.unibs.ingsoft.v5.view;

import it.unibs.ingsoft.v5.view.viewmodel.CategoriaVM;
import it.unibs.ingsoft.v5.view.viewmodel.NotificaVM;
import it.unibs.ingsoft.v5.view.viewmodel.PropostaSelezionabileVM;

import java.util.List;
import java.util.OptionalInt;

/**
 * Combined display + selection operations.
 * Returns a 0-based index into the supplied list, or {@link OptionalInt#empty()} if
 * the user cancelled (chose 0).
 * Console: prints numbered list then calls {@code acquisisciIntero}.
 * GUI: opens a modal JList dialog.
 */
public interface ISelectionView
{
    OptionalInt selezionaCategoria(List<CategoriaVM> categorie);
    OptionalInt selezionaPropostaPerIscrizione(List<PropostaSelezionabileVM> proposte);
    OptionalInt selezionaPropostaDaDisdire(List<PropostaSelezionabileVM> proposte);
    OptionalInt selezionaPropostaDaRitirare(List<PropostaSelezionabileVM> proposte);
    OptionalInt selezionaNotificaDaEliminare(List<NotificaVM> notifiche);
}
