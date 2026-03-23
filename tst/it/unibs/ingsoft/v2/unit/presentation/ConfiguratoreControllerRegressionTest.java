package it.unibs.ingsoft.v2.unit.presentation;

import it.unibs.ingsoft.v2.application.CatalogoService;
import it.unibs.ingsoft.v2.application.PropostaService;
import it.unibs.ingsoft.v2.domain.CampoBaseDefinito;
import it.unibs.ingsoft.v2.domain.Configuratore;
import it.unibs.ingsoft.v2.presentation.controller.ConfiguratoreController;
import it.unibs.ingsoft.v2.presentation.controller.PropostaController;
import it.unibs.ingsoft.v2.support.InMemoryBachecaRepository;
import it.unibs.ingsoft.v2.support.InMemoryCatalogoRepository;
import it.unibs.ingsoft.v2.support.ScriptedAppView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfiguratoreControllerRegressionTest {

    private InMemoryCatalogoRepository catalogoRepo;
    private CatalogoService catalogoService;

    @BeforeEach
    void setUp() {
        catalogoRepo = new InMemoryCatalogoRepository();
        catalogoService = new CatalogoService(catalogoRepo);
    }

    @Test
    void shouldForceBaseFieldConfigurationOnFirstLaunch() {
        ScriptedAppView view = new ScriptedAppView()
                .addYesNo(false)
                .addIntegers(0);

        ConfiguratoreController controller =
                new ConfiguratoreController(new Configuratore("mario"), view, catalogoService, createPropostaController(view));

        controller.run();

        assertEquals(CampoBaseDefinito.values().length, catalogoService.getCampiBase().size());
        assertTrue(view.containsOutput("PRIMA CONFIGURAZIONE"));
    }

    @Test
    void shouldAbortFirstConfigurationWhenCancelled() {
        ScriptedAppView view = new ScriptedAppView()
                .addCancelledYesNo(1);

        ConfiguratoreController controller =
                new ConfiguratoreController(new Configuratore("mario"), view, catalogoService, createPropostaController(view));

        controller.run();

        assertTrue(catalogoService.getCampiBase().isEmpty());
        assertTrue(view.containsOutput("Configurazione iniziale annullata."));
    }

    @Test
    void shouldReturnToMainMenuWhenCategoryMenuChoiceIsCancelled() {
        catalogoService.initiateCampiBase();

        ScriptedAppView view = new ScriptedAppView()
                .addIntegers(2)
                .addCancelledIntegers(1)
                .addIntegers(0);

        ConfiguratoreController controller =
                new ConfiguratoreController(new Configuratore("mario"), view, catalogoService, createPropostaController(view));

        controller.run();

        assertTrue(view.containsOutput("Operazione annullata."));
    }

    @Test
    void shouldCancelProposalPublicationWhenConfirmationPromptIsCancelled() {
        catalogoService.initiateCampiBase();
        catalogoService.createCategoria("Sport");

        ScriptedAppView view = new ScriptedAppView()
                .addCategorySelections(0)
                .addFormResult(validProposalValues())
                .addIntegers(4, 5, 1, 0)
                .addCancelledYesNo(1);

        PropostaController propostaController = createPropostaController(view);
        ConfiguratoreController controller =
                new ConfiguratoreController(new Configuratore("mario"), view, catalogoService, propostaController);

        controller.run();

        assertTrue(view.containsOutput("Operazione annullata."));
        assertTrue(view.containsOutput("PUBBLICA PROPOSTA"));
    }

    private PropostaController createPropostaController(ScriptedAppView view) {
        return new PropostaController(view, new PropostaService(new InMemoryBachecaRepository()));
    }

    private static java.util.Map<String, String> validProposalValues() {
        java.util.Map<String, String> values = new java.util.LinkedHashMap<>();
        values.put("Titolo", "Sport Day");
        values.put("Numero di partecipanti", "10");
        values.put("Termine ultimo di iscrizione", "15/01/2025");
        values.put("Data", "17/01/2025");
        values.put("Data conclusiva", "17/01/2025");
        values.put("Ora", "18:00");
        values.put("Luogo", "Stadio");
        values.put("Quota individuale", "12.50");
        return values;
    }
}
