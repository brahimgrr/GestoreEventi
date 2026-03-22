package it.unibs.ingsoft.v3.composition;

import it.unibs.ingsoft.v3.persistence.api.IBachecaRepository;
import it.unibs.ingsoft.v3.persistence.impl.FileBachecaRepository;
import it.unibs.ingsoft.v3.presentation.controller.PropostaController;
import it.unibs.ingsoft.v3.domain.*;
import it.unibs.ingsoft.v3.persistence.impl.*;
import it.unibs.ingsoft.v3.persistence.api.*;
import it.unibs.ingsoft.v3.persistence.dto.*;
import it.unibs.ingsoft.v3.application.*;
import it.unibs.ingsoft.v3.presentation.controller.AuthController;
import it.unibs.ingsoft.v3.presentation.controller.ConfiguratoreController;
import it.unibs.ingsoft.v3.presentation.controller.FruitoreController;
import it.unibs.ingsoft.v3.presentation.view.cli.ConsoleUI;
import it.unibs.ingsoft.v3.presentation.view.contract.IAppView;

import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

/**
 * Composition root: wires repositories, services, and controllers,
 * then runs the top-level login loop.
 */
public final class Application
{
    private static final Path DATA_CATALOGO = Path.of("data", "catalogo.json");
    private static final Path DATA_UTENTI   = Path.of("data", "utenti.json");
    private static final Path DATA_PROPOSTE = Path.of("data", "proposte.json");
    private static final Path DATA_NOTIFICHE = Path.of("data", "notifiche.json");

    public void start()
    {
        // Persistence
        ICatalogoRepository catalogoRepo      = new FileCatalogoRepository(DATA_CATALOGO);
        ICredenzialiRepository credenzialiRepo   = new FileCredenzialiRepository(DATA_UTENTI);
        IBachecaRepository bachecaRepo = new FileBachecaRepository(DATA_PROPOSTE);
        INotificaRepository  notificaRepo = new FileNotificaRepository(DATA_NOTIFICHE);

        // Services
        AuthenticationService authService      = new AuthenticationService(credenzialiRepo);
        CatalogoService          catalogoService     = new CatalogoService(catalogoRepo);
        PropostaService       propostaService  = new PropostaService(bachecaRepo);
        NotificaService notificaService = new NotificaService(notificaRepo);
        IscrizioneService iscrizioneService = new IscrizioneService(bachecaRepo, List.of(notificaService))

        // View & Controllers
        IAppView ui = new ConsoleUI(new Scanner(System.in));
        AuthController authCtrl = new AuthController(ui, authService);
        PropostaController propostaController = new PropostaController(ui, propostaService);
        ConfiguratoreController configuratoreController;
        FruitoreController fruitoreController;

        ui.header("Iniziative – Versione 3");
        do {
            ui.stampa("Accedi come:");
            ui.stampa("1) Configuratore");
            ui.stampa("2) Fruitore");
            ui.stampa("0) Esci");
            ui.newLine();
            int tipoUtente = ui.acquisisciIntero("Scelta: ", 0, 2);

            if (tipoUtente == 0)
                break;

            // Check and process expired proposals on every login
            iscService.controllaScadenzeAlAvvio();

            if (tipoUtente == 1) {
                Configuratore configuratore = authCtrl.loginConfiguratore();
                ui.stampa("Benvenuto, " + configuratore.getUsername() + "!");
                ui.newLine();

                configuratoreController = new ConfiguratoreController(configuratore, ui, catalogoService, propostaController);
                configuratoreController.run();

                // Discard unpublished valid proposals on logout
                // (requirement: "una proposta valida non pubblicata non viene salvata")
                propostaService.clearProposteValide();

                ui.stampa("Logout effettuato.");
                ui.newLine();
            } else {
                Fruitore fruitore = authCtrl.loginFruitore();

                if (fruitore == null) {
                    ui.newLine();
                    continue;
                }

                ui.stampa("Benvenuto, " + fruitore.getUsername() + "!");
                ui.newLine();


                int nuoveNotifiche = notificaService.getNotifiche(fruitore.getUsername()).size();
                if (nuoveNotifiche > 0)
                    ui.stampaAvviso("Hai " + nuoveNotifiche + " notifiche. Vai in Spazio Personale per leggerle.");
                ui.newLine();

                fruitoreController = new FruitoreController(fruitore)
                fruitoreController.run(fruitore);

                ui.stampa("Logout effettuato.");
                ui.newLine();
            }
        } while (ui.acquisisciSiNo("Vuoi accedere di nuovo?"));
    }
}
