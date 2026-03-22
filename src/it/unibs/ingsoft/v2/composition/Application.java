package it.unibs.ingsoft.v2.composition;

import it.unibs.ingsoft.v2.application.AuthenticationService;
import it.unibs.ingsoft.v2.application.CampoService;
import it.unibs.ingsoft.v2.application.CategoriaService;
import it.unibs.ingsoft.v2.application.PropostaService;
import it.unibs.ingsoft.v2.domain.Configuratore;
import it.unibs.ingsoft.v2.persistence.api.ICatalogoRepository;
import it.unibs.ingsoft.v2.persistence.api.IBachecaRepository;
import it.unibs.ingsoft.v2.persistence.api.ICredenzialiRepository;
import it.unibs.ingsoft.v2.persistence.impl.FileCatalogoRepository;
import it.unibs.ingsoft.v2.persistence.impl.FileBachecaRepository;
import it.unibs.ingsoft.v2.persistence.impl.FileCredenzialiRepository;
import it.unibs.ingsoft.v2.presentation.controller.AuthController;
import it.unibs.ingsoft.v2.presentation.controller.ConfiguratoreController;
import it.unibs.ingsoft.v2.presentation.controller.PropostaController;
import it.unibs.ingsoft.v2.presentation.view.cli.ConsoleUI;
import it.unibs.ingsoft.v2.presentation.view.contract.IAppView;

import java.nio.file.Path;
import java.util.Scanner;

/**
 * Composition root: wires all components and runs the application loop.
 */
public final class Application
{
    private static final Path DATA_CATALOGO = Path.of("data", "v2_catalogo.json");
    private static final Path DATA_UTENTI   = Path.of("data", "v2_utenti.json");
    private static final Path DATA_PROPOSTE = Path.of("data", "v2_proposte.json");

    public void start()
    {
        // Persistence
        ICatalogoRepository catalogoRepo      = new FileCatalogoRepository(DATA_CATALOGO);
        ICredenzialiRepository credenzialiRepo   = new FileCredenzialiRepository(DATA_UTENTI);
        IBachecaRepository propostaRepo = new FileBachecaRepository(DATA_PROPOSTE);

        // Services
        AuthenticationService authService      = new AuthenticationService(credenzialiRepo);
        CampoService          campoService     = new CampoService(catalogoRepo);
        CategoriaService      categoriaService = new CategoriaService(catalogoRepo, campoService);
        PropostaService       propostaService  = new PropostaService(propostaRepo, categoriaService);

        // View & Controllers
        IAppView ui = new ConsoleUI(new Scanner(System.in));
        AuthController authCtrl = new AuthController(ui, authService);
        PropostaController propostaCtrl = new PropostaController(ui, propostaService);
        ConfiguratoreController confCtrl = new ConfiguratoreController(ui, campoService, categoriaService, propostaCtrl);

        ui.header("Iniziative – Versione 2 (solo configuratore)");
        do {
            Configuratore logged = authCtrl.loginConfiguratore();
            ui.stampa("Benvenuto, " + logged.getUsername() + "!");
            ui.newLine();

            confCtrl.run();

            ui.stampa("Logout effettuato.");
            ui.newLine();

        } while (ui.acquisisciSiNo("Vuoi accedere di nuovo?"));
    }
}
