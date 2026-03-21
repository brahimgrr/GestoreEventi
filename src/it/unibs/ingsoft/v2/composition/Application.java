package it.unibs.ingsoft.v2.composition;

import it.unibs.ingsoft.v2.application.AuthenticationService;
import it.unibs.ingsoft.v2.application.CampoService;
import it.unibs.ingsoft.v2.application.CategoriaService;
import it.unibs.ingsoft.v2.application.PropostaService;
import it.unibs.ingsoft.v2.domain.Configuratore;
import it.unibs.ingsoft.v2.persistence.api.ICategoriaRepository;
import it.unibs.ingsoft.v2.persistence.api.IPropostaRepository;
import it.unibs.ingsoft.v2.persistence.api.IUtenteRepository;
import it.unibs.ingsoft.v2.persistence.impl.FileCategoriaRepository;
import it.unibs.ingsoft.v2.persistence.impl.FilePropostaRepository;
import it.unibs.ingsoft.v2.persistence.impl.FileUtenteRepository;
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
        ICategoriaRepository catRepo      = new FileCategoriaRepository(DATA_CATALOGO);
        IUtenteRepository    utenteRepo   = new FileUtenteRepository(DATA_UTENTI);
        IPropostaRepository  propostaRepo = new FilePropostaRepository(DATA_PROPOSTE);

        // Services
        AuthenticationService authService      = new AuthenticationService(utenteRepo);
        CampoService          campoService     = new CampoService(catRepo);
        CategoriaService      categoriaService = new CategoriaService(catRepo, campoService);
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
