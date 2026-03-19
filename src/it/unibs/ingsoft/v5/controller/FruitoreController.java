package it.unibs.ingsoft.v5.controller;

import it.unibs.ingsoft.v5.model.Fruitore;
import it.unibs.ingsoft.v5.model.Notifica;
import it.unibs.ingsoft.v5.model.Proposta;
import it.unibs.ingsoft.v5.service.FruitoreService;
import it.unibs.ingsoft.v5.service.IscrizioneService;
import it.unibs.ingsoft.v5.service.NotificaService;
import it.unibs.ingsoft.v5.service.PropostaService;
import it.unibs.ingsoft.v5.view.IAppView;
import it.unibs.ingsoft.v5.view.viewmodel.NotificaVM;
import it.unibs.ingsoft.v5.view.viewmodel.PropostaSelezionabileVM;
import it.unibs.ingsoft.v5.view.viewmodel.ViewModelMapper;

import java.util.List;
import java.util.OptionalInt;

/**
 * Handles all fruitore menu interactions (V5: same as V4, with NotificaService extracted).
 */
public final class FruitoreController
{
    private static final String[] MENU_FRUITORE =
            {
                    "Visualizzare la bacheca",
                    "Iscriversi a una proposta",
                    "Disdici iscrizione",
                    "Spazio personale (notifiche)"
            };

    private static final String[] MENU_NOTIFICHE =
            {
                    "Elimina una notifica"
            };

    private final IAppView          ui;
    private final PropostaService   ps;
    private final IscrizioneService is;
    private final FruitoreService   fs;
    private final NotificaService   notificaService;

    public FruitoreController(IAppView ui, PropostaService ps,
                              IscrizioneService is, FruitoreService fs,
                              NotificaService notificaService)
    {
        this.ui              = ui;
        this.ps              = ps;
        this.is              = is;
        this.fs              = fs;
        this.notificaService = notificaService;
    }

    public void run(Fruitore fruitore)
    {
        while (true)
        {
            ui.stampaMenu("MENU FRUITORE", MENU_FRUITORE);
            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_FRUITORE.length);
            ui.newLine();

            switch (choice)
            {
                case 1: menuBacheca();                 break;
                case 2: menuIscrizione(fruitore);      break;
                case 3: menuDisdici(fruitore);         break;
                case 4: menuSpazioPersonale(fruitore); break;
                case 0: return;
            }
        }
    }

    // ---------------------------------------------------------------
    // BACHECA
    // ---------------------------------------------------------------

    private void menuBacheca()
    {
        ui.header("BACHECA");
        ui.mostraBacheca(ViewModelMapper.toBachecaVM(ps.getBachecaPerCategoria(), ps::getTuttiCampi));
        ui.newLine();
        ui.pausa();
    }

    // ---------------------------------------------------------------
    // ISCRIZIONE
    // ---------------------------------------------------------------

    private void menuIscrizione(Fruitore fruitore)
    {
        ui.header("ISCRIZIONE A UNA PROPOSTA");

        List<Proposta> tutte = ps.getBacheca();

        if (tutte.isEmpty())
        {
            ui.stampa("Nessuna proposta aperta disponibile.");
            ui.newLine();
            ui.pausa();
            return;
        }

        List<PropostaSelezionabileVM> tutteVM = ViewModelMapper.toSelezionabileVMList(tutte);
        OptionalInt scelta = ui.selezionaPropostaPerIscrizione(tutteVM);

        if (scelta.isEmpty()) return;

        try
        {
            is.iscrivi(fruitore, tutte.get(scelta.getAsInt()));
            ui.stampaSuccesso("Iscrizione completata!");
        }
        catch (IllegalStateException e)
        {
            ui.stampaErrore(e.getMessage());
        }

        ui.newLine();
        ui.pausa();
    }

    // ---------------------------------------------------------------
    // DISDICI ISCRIZIONE
    // ---------------------------------------------------------------

    private void menuDisdici(Fruitore fruitore)
    {
        ui.header("DISDICI ISCRIZIONE");

        List<Proposta> iscritte = ps.getProposteIscrittePerFruitore(fruitore.getUsername());

        if (iscritte.isEmpty())
        {
            ui.stampa("Non sei iscritto a nessuna proposta aperta.");
            ui.newLine();
            ui.pausa();
            return;
        }

        ui.stampa("Le tue iscrizioni attive:");
        ui.newLine();

        List<PropostaSelezionabileVM> iscritteVM = ViewModelMapper.toSelezionabileVMList(iscritte);
        OptionalInt scelta = ui.selezionaPropostaDaDisdire(iscritteVM);

        if (scelta.isEmpty()) return;

        int idx = scelta.getAsInt();
        String titoloDa = iscritteVM.get(idx).titolo();
        if (!ui.acquisisciSiNo("Sei sicuro di voler disdire \"" + titoloDa + "\"?"))
        {
            ui.stampa("Operazione annullata.");
            ui.newLine();
            ui.pausa();
            return;
        }

        try
        {
            is.disdici(fruitore, iscritte.get(idx));
            ui.stampaSuccesso("Iscrizione disdetta con successo.");
        }
        catch (IllegalStateException e)
        {
            ui.stampaErrore(e.getMessage());
        }

        ui.newLine();
        ui.pausa();
    }

    // ---------------------------------------------------------------
    // SPAZIO PERSONALE
    // ---------------------------------------------------------------

    private void menuSpazioPersonale(Fruitore fruitore)
    {
        while (true)
        {
            ui.header("SPAZIO PERSONALE - " + fruitore.getUsername());

            List<Notifica> notifiche = notificaService.getNotifiche(fruitore.getUsername());
            List<NotificaVM> notificheVM = ViewModelMapper.toNotificaVMList(notifiche);
            ui.stampaSezione("Le tue notifiche");
            ui.mostraNotifiche(notificheVM);

            ui.newLine();
            ui.stampaMenu("", MENU_NOTIFICHE);
            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_NOTIFICHE.length);
            ui.newLine();

            switch (choice)
            {
                case 1:
                    OptionalInt idx = ui.selezionaNotificaDaEliminare(notificheVM);
                    if (idx.isPresent())
                    {
                        notificaService.eliminaNotifica(fruitore.getUsername(), idx.getAsInt());
                        ui.stampaSuccesso("Notifica eliminata.");
                    }
                    break;

                case 0:
                    return;
            }

            ui.newLine();
            ui.pausa();
        }
    }
}
