package it.unibs.ingsoft.v4.controller;

import it.unibs.ingsoft.v4.model.Configuratore;
import it.unibs.ingsoft.v4.model.Fruitore;
import it.unibs.ingsoft.v4.persistence.AppData;
import it.unibs.ingsoft.v4.persistence.DatabaseService;
import it.unibs.ingsoft.v4.service.*;
import it.unibs.ingsoft.v4.view.ConsoleUI;
import it.unibs.ingsoft.v4.view.IAppView;

import java.nio.file.Path;
import java.util.Scanner;

public final class App
{
    public void inizializzazione()
    {
        Path storage = Path.of("data", "appdata4.ser");

        DatabaseService   db          = new DatabaseService(storage);
        AppData           data        = db.loadOrCreate();
        AuthenticationService auth    = new AuthenticationService(db, data);
        CategoriaService  catService  = new CategoriaService(db, data);
        PropostaService   propService = new PropostaService(db, data);
        NotificaService   notifService = new NotificaService(db, data);
        FruitoreService   fruService  = new FruitoreService(db, data, notifService);
        IscrizioneService iscService  = new IscrizioneService(db, data, fruService);

        iscService.controllaScadenzeAlAvvio();

        try (Scanner sc = new Scanner(System.in))
        {
            IAppView ui = new ConsoleUI(sc);
            ui.header("Iniziative - Versione 4");

            AuthController          authCtrl = new AuthController(ui, auth, fruService);
            ConfiguratoreController confCtrl = new ConfiguratoreController(ui, catService, propService, iscService);
            FruitoreController      fruCtrl  = new FruitoreController(ui, propService, iscService, fruService, notifService);

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
                    int nuoveNotifiche = notifService.getNotifiche(logged.getUsername()).size();
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
