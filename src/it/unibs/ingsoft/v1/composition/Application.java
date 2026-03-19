package it.unibs.ingsoft.v1.composition;

import it.unibs.ingsoft.v1.domain.Configuratore;
import it.unibs.ingsoft.v1.persistence.dto.CatalogoData;
import it.unibs.ingsoft.v1.persistence.impl.FileCategoriaRepository;
import it.unibs.ingsoft.v1.persistence.impl.FileUtenteRepository;
import it.unibs.ingsoft.v1.persistence.api.ICategoriaRepository;
import it.unibs.ingsoft.v1.persistence.api.IUtenteRepository;
import it.unibs.ingsoft.v1.persistence.dto.UtenteData;
import it.unibs.ingsoft.v1.application.AuthenticationService;
import it.unibs.ingsoft.v1.application.CatalogoService;
import it.unibs.ingsoft.v1.presentation.controller.AuthController;
import it.unibs.ingsoft.v1.presentation.controller.ConfiguratoreController;
import it.unibs.ingsoft.v1.presentation.view.cli.ConsoleUI;
import it.unibs.ingsoft.v1.presentation.view.contract.IAppView;

import java.nio.file.Path;
import java.util.Scanner;

/**
 * Composition root: wires repositories, services, and controllers,
 * then runs the top-level login loop.
 */
public final class Application
{
    public void start()
    {
        ICategoriaRepository catRepo    = new FileCategoriaRepository(Path.of("data", "v1_catalogo.json"));
        IUtenteRepository    utenteRepo = new FileUtenteRepository(Path.of("data", "v1_utenti.json"));

        CatalogoData catalogo = catRepo.load();
        UtenteData   utenti   = utenteRepo.load();

        AuthenticationService auth        = new AuthenticationService(utenteRepo, utenti);
        CatalogoService       catService  = new CatalogoService(catRepo, catalogo);

        try (Scanner sc = new Scanner(System.in))
        {
            IAppView ui = new ConsoleUI(sc);
            ui.header("Iniziative - Versione 1 (solo configuratore)");

            AuthController authCtrl = new AuthController(ui, auth);
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
