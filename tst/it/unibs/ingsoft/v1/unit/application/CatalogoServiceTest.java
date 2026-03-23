package it.unibs.ingsoft.v1.unit.application;

import it.unibs.ingsoft.v1.application.CatalogoService;
import it.unibs.ingsoft.v1.domain.Campo;
import it.unibs.ingsoft.v1.domain.CampoBaseDefinito;
import it.unibs.ingsoft.v1.domain.Categoria;
import it.unibs.ingsoft.v1.domain.TipoDato;
import it.unibs.ingsoft.v1.support.InMemoryCatalogoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CatalogoServiceTest {

    private InMemoryCatalogoRepository repo;
    private CatalogoService service;

    @BeforeEach
    void setUp() {
        repo = new InMemoryCatalogoRepository();
        service = new CatalogoService(repo);
    }

    @Test
    void shouldInitializeBaseFieldsOnlyOnceAndPersistThem() {
        service.initiateCampiBase();

        assertEquals(CampoBaseDefinito.values().length, service.getCampiBase().size());
        assertTrue(repo.get().isCampiBaseFissati());
        assertEquals(1, repo.getSaveCount());

        IllegalStateException exception = assertThrows(IllegalStateException.class, service::initiateCampiBase);
        assertTrue(exception.getMessage().contains("gi"));
        assertEquals(CampoBaseDefinito.values().length, service.getCampiBase().size());
        assertEquals(1, repo.getSaveCount());
    }

    @Test
    void shouldAddUpdateAndRemoveCommonFieldsEvenWithoutCategories() {
        service.initiateCampiBase();

        service.addCampoComune("Descrizione", TipoDato.STRINGA, false);

        Campo beforeUpdate = service.getCampiComuni().get(0);
        assertEquals("Descrizione", beforeUpdate.getNome());
        assertFalse(beforeUpdate.isObbligatorio());

        assertTrue(service.setObbligatorietaCampoComune("Descrizione", true));

        Campo afterUpdate = service.getCampiComuni().get(0);
        assertTrue(afterUpdate.isObbligatorio());
        assertTrue(service.removeCampoComune("Descrizione"));
        assertFalse(service.removeCampoComune("Descrizione"));
        assertTrue(service.getCampiComuni().isEmpty());
    }

    @Test
    void shouldAllowCommonFieldCreationAfterCategoriesExist() {
        service.initiateCampiBase();
        service.createCategoria("Sport");

        service.addCampoComune("Sponsor", TipoDato.STRINGA, false);

        assertEquals(1, service.getCategorie().size());
        assertEquals(List.of("Sponsor"), service.getCampiComuni().stream().map(Campo::getNome).toList());
    }

    @Test
    void shouldStartWithNoCategoriesAndRejectDuplicateNames() {
        assertTrue(service.getCategorie().isEmpty());

        service.createCategoria("Sport");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createCategoria("sport")
        );

        assertTrue(exception.getMessage().contains("Categoria"));
        assertEquals(1, service.getCategorie().size());
    }

    @Test
    void shouldAddUpdateAndRemoveSpecificFields() {
        service.initiateCampiBase();
        Categoria categoria = service.createCategoria("Sport");

        service.addCampoSpecifico(categoria.getNome(), "Attrezzatura", TipoDato.STRINGA, false);

        Campo specifico = service.getCategorie().get(0).getCampiSpecifici().get(0);
        assertEquals("Attrezzatura", specifico.getNome());
        assertFalse(specifico.isObbligatorio());

        assertTrue(service.setObbligatorietaCampoSpecifico("Sport", "Attrezzatura", true));
        assertTrue(service.getCategorie().get(0).getCampiSpecifici().get(0).isObbligatorio());
        assertTrue(service.removeCampoSpecifico("Sport", "Attrezzatura"));
        assertFalse(service.removeCampoSpecifico("Sport", "Attrezzatura"));
        assertTrue(service.getCategorie().get(0).getCampiSpecifici().isEmpty());
    }

    @Test
    void shouldRemoveCategoryTogetherWithItsSpecificFields() {
        service.initiateCampiBase();
        service.createCategoria("Sport");
        service.addCampoSpecifico("Sport", "Attrezzatura", TipoDato.STRINGA, true);

        assertTrue(service.removeCategoria("Sport"));

        assertTrue(service.getCategorie().isEmpty());
        assertFalse(service.removeCategoria("Sport"));
    }

    @Test
    void shouldRejectDuplicateFieldNamesAcrossBaseAndCommonFields() {
        service.initiateCampiBase();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.addCampoComune("Titolo", TipoDato.STRINGA, true)
        );

        assertTrue(exception.getMessage().contains("Campo"));
    }

    @Test
    void shouldHandleMissingFieldOrCategoryUpdatesSafely() {
        service.initiateCampiBase();
        service.createCategoria("Sport");

        assertFalse(service.setObbligatorietaCampoComune("Mancante", true));
        assertThrows(IllegalArgumentException.class,
                () -> service.removeCampoSpecifico("Mancante", "Campo"));
        assertThrows(IllegalArgumentException.class,
                () -> service.setObbligatorietaCampoSpecifico("Mancante", "Campo", true));
    }
}
