package it.unibs.ingsoft.v5.presentation.controller;

import it.unibs.ingsoft.v5.application.CatalogoService;
import it.unibs.ingsoft.v5.application.PropostaService;
import it.unibs.ingsoft.v5.application.batch.BatchImportService;
import it.unibs.ingsoft.v5.domain.AppConstants;
import it.unibs.ingsoft.v5.support.InMemoryBachecaRepository;
import it.unibs.ingsoft.v5.support.InMemoryCatalogoRepository;
import it.unibs.ingsoft.v5.support.ScriptedAppView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BatchImportControllerTest {

    private Clock originalClock;
    private ScriptedAppView ui;
    private BatchImportController controller;

    @BeforeEach
    void setUp() {
        originalClock = AppConstants.clock;
        AppConstants.clock = Clock.fixed(Instant.parse("2026-04-01T09:00:00Z"), ZoneId.of("Europe/Rome"));

        CatalogoService catalogoService = new CatalogoService(new InMemoryCatalogoRepository());
        PropostaService propostaService = new PropostaService(new InMemoryBachecaRepository());
        BatchImportService importService = new BatchImportService(catalogoService, propostaService);

        ui = new ScriptedAppView();
        controller = new BatchImportController(ui, importService);
    }

    @AfterEach
    void tearDown() {
        AppConstants.clock = originalClock;
    }

    @Test
    void avviaImportazione_withValidFile_showsSummaryAndPublishHint() {
        ui.addStrings("data\\batch_valid.json");

        controller.avviaImportazione();

        assertTrue(ui.containsOutput("RISULTATO IMPORTAZIONE"));
        assertTrue(ui.containsOutput("Campi comuni importati: 2"));
        assertTrue(ui.containsOutput("Le proposte valide possono essere pubblicate"));
        assertTrue(ui.containsOutput("Importazione completata: 6 elementi importati."));
        assertTrue(ui.containsOutput("PAUSA"));
    }

    @Test
    void avviaImportazione_withBlankPath_showsValidationError() {
        ui.addStrings("   ");

        controller.avviaImportazione();

        assertTrue(ui.containsOutput("Percorso non valido."));
        assertTrue(ui.containsOutput("PAUSA"));
    }

    @Test
    void avviaImportazione_whenCancelled_showsCancelledMessage() {
        ui.addStrings("annulla");

        controller.avviaImportazione();

        assertTrue(ui.containsOutput("Operazione annullata."));
        assertTrue(ui.containsOutput("PAUSA"));
    }
}
