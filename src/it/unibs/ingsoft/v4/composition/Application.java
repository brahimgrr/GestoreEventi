package it.unibs.ingsoft.v4.composition;

import it.unibs.ingsoft.v4.application.*;
import it.unibs.ingsoft.v4.domain.AppConstants;
import it.unibs.ingsoft.v4.domain.Configuratore;
import it.unibs.ingsoft.v4.domain.Fruitore;
import it.unibs.ingsoft.v4.persistence.api.IBachecaRepository;
import it.unibs.ingsoft.v4.persistence.api.ICatalogoRepository;
import it.unibs.ingsoft.v4.persistence.api.ICredenzialiRepository;
import it.unibs.ingsoft.v4.persistence.api.ISpazioPersonaleRepository;
import it.unibs.ingsoft.v4.persistence.impl.FileBachecaRepository;
import it.unibs.ingsoft.v4.persistence.impl.FileCatalogoRepository;
import it.unibs.ingsoft.v4.persistence.impl.FileCredenzialiRepository;
import it.unibs.ingsoft.v4.persistence.impl.FileSpazioPersonaleRepository;
import it.unibs.ingsoft.v4.presentation.controller.*;
import it.unibs.ingsoft.v4.presentation.view.cli.ConsoleUI;
import it.unibs.ingsoft.v4.presentation.view.contract.IAppView;

import java.nio.file.Path;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Composition root: wires all components and runs the application loop.
 */
public final class Application
{
    private static final Path DATA_CATALOGO   = Path.of("data", "v4_catalogo.json");
    private static final Path DATA_UTENTI     = Path.of("data", "v4_utenti.json");
    private static final Path DATA_PROPOSTE   = Path.of("data", "v4_proposte.json");
    private static final Path DATA_NOTIFICHE  = Path.of("data", "v4_notifiche.json");
    private ScheduledExecutorService midnightScheduler;

    public void start()
    {
        // Persistence
        ICatalogoRepository catalogoRepo      = new FileCatalogoRepository(DATA_CATALOGO);
        ICredenzialiRepository credenzialiRepo   = new FileCredenzialiRepository(DATA_UTENTI);
        IBachecaRepository propostaRepo = new FileBachecaRepository(DATA_PROPOSTE);
        ISpazioPersonaleRepository spazioRepo = new FileSpazioPersonaleRepository(DATA_NOTIFICHE);

        // Services
        AuthenticationService authService      = new AuthenticationService(credenzialiRepo);
        CatalogoService catalogoService     = new CatalogoService(catalogoRepo);
        PropostaService propostaService  = new PropostaService(propostaRepo);
        NotificationService notifService = new NotificationService(spazioRepo);
        StateTransitionService stateService = new StateTransitionService(propostaRepo, notifService);
        IscrizioneService iscrizioneService = new IscrizioneService(propostaRepo, stateService);

        // Esegui la conciliazione degli stati (mezzanotte)
        stateService.controllaScadenze();
        startMidnightScheduler(stateService);

        // View & Controllers
        IAppView ui = new ConsoleUI(new Scanner(System.in));
        AuthController authCtrl = new AuthController(ui, authService);
        PropostaController propostaController = new PropostaController(ui, propostaService);

        ui.header("Gestore Eventi – Versione 3");

        while (true) {
            ui.stampaMenu("MENU DI ACCESSO", new String[]{
                "Accedi come Configuratore",
                "Accedi come Fruitore",
                "Registrati come Fruitore"
            }, "Esci dall'applicazione");
            
            int choice = ui.acquisisciIntero("Scelta: ", 0, 3);
            
            if (choice == 0) {
                ui.stampa("Arrivederci!");
                stopMidnightScheduler();
                break;
            } else if (choice == 1) {
                Configuratore configuratore = authCtrl.loginConfiguratore();
                if (configuratore != null) {
                    ui.stampa("Benvenuto Configuratore, " + configuratore.getUsername() + "!");
                    ui.newLine();
                    new ConfiguratoreController(configuratore, ui, catalogoService, propostaController).run();
                    
                    // Discard unpublished valid proposals on logout
                    propostaService.clearProposteValide();
                    ui.stampa("Logout configuratore effettuato.");
                    ui.newLine();
                }
            } else if (choice == 2) {
                Fruitore fruitore = authCtrl.loginFruitore();
                if (fruitore != null) {
                    SpazioPersonaleController spc = new SpazioPersonaleController(fruitore, ui, notifService);
                    new FruitoreController(fruitore, ui, propostaService, iscrizioneService, spc).run();
                    ui.stampa("Logout fruitore effettuato.");
                    ui.newLine();
                }
            } else if (choice == 3) {
                authCtrl.registraFruitore();
            }
        }
    }

    private void startMidnightScheduler(StateTransitionService stateService)
    {
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, "state-transition-midnight");
            thread.setDaemon(true);
            return thread;
        };

        midnightScheduler = Executors.newSingleThreadScheduledExecutor(threadFactory);
        scheduleNextMidnightCheck(stateService);
    }

    private void stopMidnightScheduler()
    {
        if (midnightScheduler != null) {
            midnightScheduler.shutdownNow();
        }
    }

    private long millisUntilNextMidnight()
    {
        ZonedDateTime now = ZonedDateTime.now(AppConstants.clock);
        ZonedDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(now.getZone());
        return Duration.between(now, nextMidnight).toMillis();
    }

    private void scheduleNextMidnightCheck(StateTransitionService stateService)
    {
        if (midnightScheduler == null || midnightScheduler.isShutdown()) {
            return;
        }
        long delay = millisUntilNextMidnight();
        midnightScheduler.schedule(() -> {
            stateService.controllaScadenze();
            scheduleNextMidnightCheck(stateService);
        }, delay, TimeUnit.MILLISECONDS);
    }
}
