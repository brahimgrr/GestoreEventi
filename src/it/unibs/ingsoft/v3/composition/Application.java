package it.unibs.ingsoft.v3.composition;

import it.unibs.ingsoft.v3.domain.Configuratore;
import it.unibs.ingsoft.v3.domain.Fruitore;
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
    public void inizializzazione()
    {
        ICategoriaRepository catRepo      = new FileCategoriaRepository(Path.of("data", "v3_catalogo.json"));
        IUtenteRepository    utenteRepo   = new FileUtenteRepository(Path.of("data", "v3_utenti.json"));
        INotificaRepository  notificaRepo = new FileNotificaRepository(Path.of("data", "v3_notifiche.json"));
        IPropostaRepository  propostaRepo = new FilePropostaRepository(Path.of("data", "v3_proposte.json"));

        CatalogoData catalogo     = catRepo.load();
        UsersData    utenti       = utenteRepo.load();
        NotificaData notificaData = notificaRepo.load();
        PropostaData proposteData = propostaRepo.load();

        AuthenticationService auth            = new AuthenticationService(utenteRepo, utenti);
        CampoService          campoService    = new CampoService(catRepo, catalogo);
        CategoriaService      catService      = new CategoriaService(catRepo, catalogo, campoService);
        PropostaService       propService     = new PropostaService(catalogo, propostaRepo, proposteData);
        NotificaService       notificaService = new NotificaService(notificaRepo, notificaData);
        IscrizioneService     iscService      = new IscrizioneService(propostaRepo, proposteData,
                                                                       List.of(notificaService));

        try (Scanner sc = new Scanner(System.in))
        {
            IAppView ui = new ConsoleUI(sc);
            ui.header("Iniziative - Versione 3");

            AuthController          authCtrl = new AuthController(ui, auth);
            ConfiguratoreController confCtrl = new ConfiguratoreController(ui, campoService, catService, propService);
            FruitoreController      fruCtrl  = new FruitoreController(ui, propService, iscService, notificaService);

            while (true)
            {
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

                if (tipoUtente == 1)
                {
                    Configuratore logged = authCtrl.loginConfiguratore();
                    ui.stampa("Benvenuto, " + logged.getUsername());
                    ui.newLine();
                    confCtrl.run();
                    ui.stampa("Logout effettuato.");
                    ui.newLine();
                }
                else
                {
                    Fruitore logged = authCtrl.loginFruitore();

                    if (logged == null)
                    {
                        ui.newLine();
                        continue;
                    }

                    ui.stampa("Benvenuto, " + logged.getUsername());
                    int nuoveNotifiche = notificaService.getNotifiche(logged.getUsername()).size();
                    if (nuoveNotifiche > 0)
                        ui.stampaAvviso("Hai " + nuoveNotifiche + " notifiche. Vai in Spazio Personale per leggerle.");
                    ui.newLine();
                    fruCtrl.run(logged);
                    ui.stampa("Logout effettuato.");
                    ui.newLine();
                }
            }
        }
    }
}
