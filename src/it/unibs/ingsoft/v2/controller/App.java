package it.unibs.ingsoft.v2.controller;

import it.unibs.ingsoft.v2.model.Configuratore;
import it.unibs.ingsoft.v2.persistence.AppData;
import it.unibs.ingsoft.v2.persistence.DatabaseService;
import it.unibs.ingsoft.v2.service.AuthenticationService;
import it.unibs.ingsoft.v2.service.CategoriaService;
import it.unibs.ingsoft.v2.service.PropostaService;
import it.unibs.ingsoft.v2.view.ConsoleUI;
import it.unibs.ingsoft.v2.view.IAppView;

import java.nio.file.Path;
import java.util.Scanner;

/**
 * Composition root: wires services and controllers, runs the top-level login loop.
 */
public final class App
{
    public void inizializzazione()
    {
        Path storage = Path.of("data", "appdata2.ser");

        DatabaseService       db          = new DatabaseService(storage);
        AppData               data        = db.loadOrCreate();
        AuthenticationService auth        = new AuthenticationService(db, data);
        CategoriaService      catService  = new CategoriaService(db, data);
        PropostaService       propService = new PropostaService(db, data);

        try (Scanner sc = new Scanner(System.in))
        {
            IAppView ui = new ConsoleUI(sc);
            ui.header("Iniziative - Versione 2");

            AuthController          authCtrl = new AuthController(ui, auth);
            ConfiguratoreController confCtrl = new ConfiguratoreController(ui, catService, propService);

            while (true)
            {
                Configuratore logged = authCtrl.loginConfiguratore();
                ui.stampa("Benvenuto, " + logged.getUsername());
                ui.newLine();

                confCtrl.run();

                ui.stampa("Logout effettuato.");
                ui.newLine();
            }
        }
    }
}
