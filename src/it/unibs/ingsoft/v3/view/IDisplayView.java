package it.unibs.ingsoft.v3.view;

import it.unibs.ingsoft.v3.view.viewmodel.NotificaVM;
import it.unibs.ingsoft.v3.view.viewmodel.PropostaVM;

import java.util.List;
import java.util.Map;

/**
 * Pure display operations — all parameters are view-models, no domain objects.
 * Console: formatted text output.
 * GUI: JTable / JList / JPanel.
 */
public interface IDisplayView
{
    void mostraBacheca(Map<String, List<PropostaVM>> bachecaPerCategoria);
    void mostraArchivio(List<PropostaVM> archivio);
    void mostraNotifiche(List<NotificaVM> notifiche);
    void mostraRiepilogoProposta(PropostaVM proposta);
}
