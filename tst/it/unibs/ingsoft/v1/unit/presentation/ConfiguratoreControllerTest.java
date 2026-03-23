package it.unibs.ingsoft.v1.unit.presentation;

import it.unibs.ingsoft.v1.application.CatalogoService;
import it.unibs.ingsoft.v1.domain.Campo;
import it.unibs.ingsoft.v1.domain.CampoBaseDefinito;
import it.unibs.ingsoft.v1.domain.Configuratore;
import it.unibs.ingsoft.v1.domain.TipoDato;
import it.unibs.ingsoft.v1.presentation.controller.ConfiguratoreController;
import it.unibs.ingsoft.v1.support.InMemoryCatalogoRepository;
import it.unibs.ingsoft.v1.support.ScriptedAppView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfiguratoreControllerTest {

    private InMemoryCatalogoRepository repo;
    private CatalogoService catalogoService;
    private Configuratore configuratore;

    @BeforeEach
    void setUp() {
        repo = new InMemoryCatalogoRepository();
        catalogoService = new CatalogoService(repo);
        configuratore = new Configuratore("mario");
    }

    @Test
    void shouldForceBaseFieldInitializationBeforeMainMenu() {
        ScriptedAppView view = new ScriptedAppView()
                .addYesNo(false)
                .addIntegers(0);

        ConfiguratoreController controller = new ConfiguratoreController(configuratore, view, catalogoService);
        controller.run();

        assertEquals(CampoBaseDefinito.values().length, catalogoService.getCampiBase().size());
        assertTrue(view.containsOutput("PRIMA CONFIGURAZIONE"));
        assertEquals(1, repo.getSaveCount());
    }

    @Test
    void shouldDisplayBaseCommonAndSpecificFieldsInVisualization() {
        catalogoService.initiateCampiBase();
        catalogoService.addCampoComune("Sponsor", TipoDato.STRINGA, false);
        catalogoService.createCategoria("Sport");
        catalogoService.addCampoSpecifico("Sport", "Attrezzatura", TipoDato.STRINGA, true);

        ScriptedAppView view = new ScriptedAppView()
                .addIntegers(3, 0);

        ConfiguratoreController controller = new ConfiguratoreController(configuratore, view, catalogoService);
        controller.run();

        assertEquals(2, view.getPrintedCampiBatches().size());
        assertEquals(CampoBaseDefinito.values().length, view.getPrintedCampiBatches().get(0).size());
        assertEquals(List.of("Sponsor"), view.getPrintedCampiBatches().get(1).stream().map(Campo::getNome).toList());
        assertEquals(1, view.getPrintedCategorieBatches().size());
        assertEquals("Attrezzatura",
                view.getPrintedCategorieBatches().get(0).get(0).getCampiSpecifici().get(0).getNome());
    }

    @Test
    void shouldRemoveExistingCategoryThroughMenu() {
        catalogoService.initiateCampiBase();
        catalogoService.createCategoria("Sport");
        catalogoService.addCampoSpecifico("Sport", "Attrezzatura", TipoDato.STRINGA, true);

        ScriptedAppView view = new ScriptedAppView()
                .addIntegers(2, 2, 1, 0, 0)
                .addYesNo(true);

        ConfiguratoreController controller = new ConfiguratoreController(configuratore, view, catalogoService);
        controller.run();

        assertTrue(catalogoService.getCategorie().isEmpty());
        assertTrue(view.containsOutput("Categoria rimossa."));
    }

    @Test
    void shouldChangeCommonFieldMandatoryFlagWithoutCategories() {
        catalogoService.initiateCampiBase();
        catalogoService.addCampoComune("Descrizione", TipoDato.STRINGA, false);

        ScriptedAppView view = new ScriptedAppView()
                .addIntegers(1, 3, 1, 0, 0)
                .addYesNo(true);

        ConfiguratoreController controller = new ConfiguratoreController(configuratore, view, catalogoService);
        controller.run();

        Campo updated = catalogoService.getCampiComuni().get(0);
        assertTrue(updated.isObbligatorio());
        assertTrue(view.containsOutput("Aggiornato."));
    }
}
