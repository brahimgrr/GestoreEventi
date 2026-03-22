package it.unibs.ingsoft.v1.composition;

import it.unibs.ingsoft.v1.domain.Configuratore;
import it.unibs.ingsoft.v1.persistence.impl.FileCatalogoRepository;
import it.unibs.ingsoft.v1.persistence.impl.FileCredenzialiRepository;
import it.unibs.ingsoft.v1.persistence.api.ICatalogoRepository;
import it.unibs.ingsoft.v1.persistence.api.ICredenzialiRepository;
import it.unibs.ingsoft.v1.application.AuthenticationService;
import it.unibs.ingsoft.v1.application.CatalogoService;
import it.unibs.ingsoft.v1.presentation.controller.AuthController;
import it.unibs.ingsoft.v1.presentation.controller.ConfiguratoreController;
import it.unibs.ingsoft.v1.presentation.view.cli.ConsoleUI;
import it.unibs.ingsoft.v1.presentation.view.contract.IAppView;

import java.nio.file.Path;
import java.util.Scanner;

/**
 * Composition root: wires all components and runs the application loop.
 */
public final class Application
{
    private static final Path DATA_CATALOGO = Path.of("data", "v1_catalogo.json");
    private static final Path DATA_UTENTI   = Path.of("data", "v1_utenti.json");

    public void start()
    {
        // Persistence
        ICatalogoRepository catRepo      = new FileCatalogoRepository(DATA_CATALOGO);
        ICredenzialiRepository utenteRepo   = new FileCredenzialiRepository(DATA_UTENTI);

        // Services
        AuthenticationService authService      = new AuthenticationService(utenteRepo);
        CatalogoService       catalogoService  = new CatalogoService(catRepo);

        // View & Controllers
        IAppView ui = new ConsoleUI(new Scanner(System.in));
        AuthController authCtrl = new AuthController(ui, authService);
        ConfiguratoreController confCtrl;

        ui.header("Iniziative - Versione 1 (solo configuratore)");
        do {
            Configuratore logged = authCtrl.loginConfiguratore();
            ui.stampa("Benvenuto, " + logged.getUsername() + "!");
            ui.newLine();

            confCtrl = new ConfiguratoreController(logged, ui, catalogoService);
            confCtrl.run();

            ui.stampa("Logout effettuato.");
            ui.newLine();

        } while (ui.acquisisciSiNo("Vuoi accedere di nuovo?"));
    }
}
