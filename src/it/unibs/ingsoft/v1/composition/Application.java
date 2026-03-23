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
import it.unibs.ingsoft.v1.presentation.view.contract.OperationCancelledException;

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
        ICatalogoRepository catalogoRepo      = new FileCatalogoRepository(DATA_CATALOGO);
        ICredenzialiRepository credenzialiRepo   = new FileCredenzialiRepository(DATA_UTENTI);

        // Services
        AuthenticationService authService      = new AuthenticationService(credenzialiRepo);
        CatalogoService       catalogoService  = new CatalogoService(catalogoRepo);

        // View & Controllers
        IAppView ui = new ConsoleUI(new Scanner(System.in));
        AuthController authCtrl = new AuthController(ui, authService);
        ConfiguratoreController configuratoreController;

        ui.header("Iniziative - Versione 1 (solo configuratore)");
        while (true) {
            try
            {
                Configuratore configuratore = authCtrl.loginConfiguratore();
                ui.stampa("Benvenuto, " + configuratore.getUsername() + "!");
                ui.newLine();

                configuratoreController = new ConfiguratoreController(configuratore, ui, catalogoService);
                configuratoreController.run();

                ui.stampa("Logout effettuato.");
                ui.newLine();
            }
            catch (OperationCancelledException e)
            {
                ui.stampaInfo("Operazione annullata. Uscita dall'applicazione.");
                return;
            }

            try
            {
                if (!ui.acquisisciSiNo("Vuoi accedere di nuovo?"))
                    return;
            }
            catch (OperationCancelledException e)
            {
                ui.stampaInfo("Operazione annullata. Uscita dall'applicazione.");
                return;
            }
        }
    }
}
