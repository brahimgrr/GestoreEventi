package it.unibs.ingsoft.v1.controller;

import it.unibs.ingsoft.v1.model.Configuratore;
import it.unibs.ingsoft.v1.persistence.AppData;
import it.unibs.ingsoft.v1.persistence.DatabaseService;
import it.unibs.ingsoft.v1.service.AuthenticationService;
import it.unibs.ingsoft.v1.service.CategoriaService;
import it.unibs.ingsoft.v1.view.ConsoleUI;
import it.unibs.ingsoft.v1.view.IAppView;

import java.nio.file.Path;
import java.util.Scanner;

/**
 * Composition root: wires services and controllers, runs the top-level login loop.
 */
public final class App
{
    public void inizializzazione()
    {
        Path storage = Path.of("data", "appdata.ser");

        DatabaseService db      = new DatabaseService(storage);
        AppData         data    = db.loadOrCreate();

        AuthenticationService  auth       = new AuthenticationService(db, data);
        CategoriaService       catService = new CategoriaService(db, data);

        try (Scanner sc = new Scanner(System.in))
        {
            IAppView ui = new ConsoleUI(sc);
            ui.header("Iniziative - Versione 1 (solo configuratore)");

            AuthController          authCtrl = new AuthController(ui, auth);
            ConfiguratoreController confCtrl = new ConfiguratoreController(ui, catService);

            do
            {
                Configuratore logged = authCtrl.loginConfiguratore();
                ui.stampa("Benvenuto, " + logged.getUsername() + "!");
                ui.newLine();

                confCtrl.run();

                ui.stampa("Logout effettuato.");
                ui.newLine();

            } while (ui.acquisisciSiNo("Vuoi accedere di nuovo?"));
        }
    }
}
