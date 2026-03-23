package it.unibs.ingsoft.v3.composition;

import it.unibs.ingsoft.v3.domain.Configuratore;
import it.unibs.ingsoft.v3.application.AuthenticationService;
import it.unibs.ingsoft.v3.application.CatalogoService;
import it.unibs.ingsoft.v3.application.PropostaService;
import it.unibs.ingsoft.v3.persistence.api.IBachecaRepository;
import it.unibs.ingsoft.v3.persistence.api.ICatalogoRepository;
import it.unibs.ingsoft.v3.persistence.api.ICredenzialiRepository;
import it.unibs.ingsoft.v3.persistence.impl.FileBachecaRepository;
import it.unibs.ingsoft.v3.persistence.impl.FileCatalogoRepository;
import it.unibs.ingsoft.v3.persistence.impl.FileCredenzialiRepository;
import it.unibs.ingsoft.v3.presentation.controller.AuthController;
import it.unibs.ingsoft.v3.presentation.controller.ConfiguratoreController;
import it.unibs.ingsoft.v3.presentation.controller.PropostaController;
import it.unibs.ingsoft.v3.presentation.view.cli.ConsoleUI;
import it.unibs.ingsoft.v3.presentation.view.contract.IAppView;

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
        CatalogoService catalogoService     = new CatalogoService(catalogoRepo);
        PropostaService propostaService  = new PropostaService(propostaRepo);

        // View & Controllers
        IAppView ui = new ConsoleUI(new Scanner(System.in));
        AuthController authCtrl = new AuthController(ui, authService);
        PropostaController propostaController = new PropostaController(ui, propostaService);
        ConfiguratoreController configuratoreController;

        ui.header("Iniziative – Versione 2 (solo configuratore)");
        do {
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

        } while (ui.acquisisciSiNo("Vuoi accedere di nuovo?"));
    }
}
