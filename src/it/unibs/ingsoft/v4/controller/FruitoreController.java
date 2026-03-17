package it.unibs.ingsoft.v4.controller;

import it.unibs.ingsoft.v4.model.Fruitore;
import it.unibs.ingsoft.v4.model.Notifica;
import it.unibs.ingsoft.v4.model.Proposta;
import it.unibs.ingsoft.v4.service.FruitoreService;
import it.unibs.ingsoft.v4.service.IscrizioneService;
import it.unibs.ingsoft.v4.service.NotificaService;
import it.unibs.ingsoft.v4.service.PropostaService;
import it.unibs.ingsoft.v4.view.IAppView;

import java.util.List;

/**
 * Handles all fruitore menu interactions (V4: adds menuDisdici).
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
        this.ui             = ui;
        this.ps             = ps;
        this.is             = is;
        this.fs             = fs;
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
        ui.mostraBacheca(ps.getBachecaPerCategoria(), ps::getTuttiCampi);
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

        ui.mostraPropostePerIscrizione(tutte);

        int scelta = ui.acquisisciIntero("Scegli proposta (0 per annullare): ", 0, tutte.size());

        if (scelta == 0)
            return;

        try
        {
            is.iscrivi(fruitore, tutte.get(scelta - 1));
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

        ui.stampaProposteDisdici(iscritte);

        ui.newLine();
        int scelta = ui.acquisisciIntero(
                "Scegli proposta da disdire (0 per annullare): ", 0, iscritte.size());

        if (scelta == 0)
            return;

        String titoloDa = iscritte.get(scelta - 1).getValoriCampi()
                .getOrDefault(PropostaService.CAMPO_TITOLO, "senza titolo");
        if (!ui.acquisisciSiNo("Sei sicuro di voler disdire \"" + titoloDa + "\"?"))
        {
            ui.stampa("Operazione annullata.");
            ui.newLine();
            ui.pausa();
            return;
        }

        try
        {
            is.disdici(fruitore, iscritte.get(scelta - 1));
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
            ui.stampaSezione("Le tue notifiche");
            ui.mostraNotifiche(notifiche);

            ui.newLine();
            ui.stampaMenu("", MENU_NOTIFICHE);
            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_NOTIFICHE.length);
            ui.newLine();

            switch (choice)
            {
                case 1:
                    if (notifiche.isEmpty())
                    {
                        ui.stampa("Nessuna notifica da eliminare.");
                        break;
                    }
                    int idx = ui.acquisisciIntero("Numero notifica da eliminare: ", 1, notifiche.size());
                    try
                    {
                        notificaService.eliminaNotifica(fruitore.getUsername(), idx - 1);
                        ui.stampa("Notifica eliminata.");
                    }
                    catch (IndexOutOfBoundsException e)
                    {
                        ui.stampa("Errore nell'eliminazione: " + e.getMessage());
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
