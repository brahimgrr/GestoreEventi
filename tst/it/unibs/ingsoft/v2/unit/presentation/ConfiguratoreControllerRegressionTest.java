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

        PropostaController propostaController =
                new PropostaController(view, new PropostaService(new InMemoryBachecaRepository()));
        ConfiguratoreController controller =
                new ConfiguratoreController(new Configuratore("mario"), view, catalogoService, propostaController);

        controller.run();

        assertEquals(CampoBaseDefinito.values().length, catalogoService.getCampiBase().size());
        assertTrue(view.containsOutput("PRIMA CONFIGURAZIONE"));
    }
}
