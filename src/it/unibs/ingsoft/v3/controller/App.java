package it.unibs.ingsoft.v3.controller;

import it.unibs.ingsoft.v3.model.Configuratore;
import it.unibs.ingsoft.v3.model.Fruitore;
import it.unibs.ingsoft.v3.persistence.AppData;
import it.unibs.ingsoft.v3.persistence.DatabaseService;
import it.unibs.ingsoft.v3.service.*;
import it.unibs.ingsoft.v3.view.ConsoleUI;
import it.unibs.ingsoft.v3.view.IAppView;

import java.nio.file.Path;
import java.util.Scanner;

/**
 * Composition root: wires services and controllers, runs the top-level login loop.
 */
public final class App
{
    public void inizializzazione()
    {
        Path storage = Path.of("data", "appdata3.ser");

        DatabaseService       db              = new DatabaseService(storage);
        AppData               data            = db.loadOrCreate();
        AuthenticationService auth            = new AuthenticationService(db, data);
        CategoriaService      catService      = new CategoriaService(db, data);
        PropostaService       propService     = new PropostaService(db, data);
        NotificaService       notificaService = new NotificaService(db, data);
        FruitoreService       fruService      = new FruitoreService(db, data, notificaService);
        IscrizioneService     iscService      = new IscrizioneService(db, data, fruService);

        iscService.controllaScadenzeAlAvvio();

        try (Scanner sc = new Scanner(System.in))
        {
            IAppView ui = new ConsoleUI(sc);
            ui.header("Iniziative - Versione 3");

            AuthController          authCtrl = new AuthController(ui, auth, fruService);
            ConfiguratoreController confCtrl = new ConfiguratoreController(ui, catService, propService);
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
}
