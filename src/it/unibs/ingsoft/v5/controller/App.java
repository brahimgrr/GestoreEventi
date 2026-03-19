package it.unibs.ingsoft.v5.controller;

import it.unibs.ingsoft.v5.model.Configuratore;
import it.unibs.ingsoft.v5.model.Fruitore;
import it.unibs.ingsoft.v5.persistence.*;
import it.unibs.ingsoft.v5.service.*;
import it.unibs.ingsoft.v5.view.ConsoleUI;
import it.unibs.ingsoft.v5.view.IAppView;

import java.nio.file.Path;
import java.util.Scanner;

/**
 * Composition root: wires repositories, services, and controllers,
 * then runs the top-level login loop.
 */
public final class App
{
    public void inizializzazione()
    {
        ICategoriaRepository catRepo      = new FileCategoriaRepository(Path.of("data", "v5_catalogo.ser"));
        IUtenteRepository    utenteRepo   = new FileUtenteRepository(Path.of("data", "v5_utenti.ser"));
        IFruitoreRepository  fruitoreRepo = new FileFruitoreRepository(Path.of("data", "v5_fruitori.ser"));
        INotificaRepository  notificaRepo = new FileNotificaRepository(Path.of("data", "v5_notifiche.ser"));
        IPropostaRepository  propostaRepo = new FilePropostaRepository(Path.of("data", "v5_proposte.ser"));

        CatalogoData catalogo     = catRepo.load();
        UtenteData   utenti       = utenteRepo.load();
        FruitoreData fData        = fruitoreRepo.load();
        NotificaData notificaData = notificaRepo.load();
        PropostaData proposteData = propostaRepo.load();

        AuthenticationService auth            = new AuthenticationService(utenteRepo, utenti, fData);
        CampoService          campoService    = new CampoService(catRepo, catalogo);
        CategoriaService      catService      = new CategoriaService(catRepo, catalogo, campoService);
        PropostaService       propService     = new PropostaService(catalogo, propostaRepo, proposteData);
        NotificaService       notificaService = new NotificaService(notificaRepo, notificaData);
        FruitoreService       fruService      = new FruitoreService(fruitoreRepo, fData, utenti, notificaService);
        IscrizioneService     iscService      = new IscrizioneService(propostaRepo, proposteData,
                                                                       notificaRepo, notificaData, fruService);

        IUnitOfWork unitOfWork = new FileUnitOfWork(catRepo, catalogo, propostaRepo, proposteData);

        BatchImportService batchService = new BatchImportService(campoService, catService, propService, unitOfWork);

        iscService.controllaScadenzeAlAvvio();

        try (Scanner sc = new Scanner(System.in))
        {
            IAppView ui = new ConsoleUI(sc);
            ui.header("Iniziative - Versione 5");

            AuthController          authCtrl = new AuthController(ui, auth, fruService);
            ConfiguratoreController confCtrl = new ConfiguratoreController(ui, campoService, catService, propService, iscService, batchService);
            FruitoreController      fruCtrl  = new FruitoreController(ui, propService, iscService, fruService, notificaService);

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

    public static void main(String[] args)
    {
        new App().inizializzazione();
    }
}
