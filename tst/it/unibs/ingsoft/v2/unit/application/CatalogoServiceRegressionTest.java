package it.unibs.ingsoft.v2.unit.application;

import it.unibs.ingsoft.v2.application.CatalogoService;
import it.unibs.ingsoft.v2.domain.Campo;
import it.unibs.ingsoft.v2.domain.CampoBaseDefinito;
import it.unibs.ingsoft.v2.domain.TipoDato;
import it.unibs.ingsoft.v2.support.InMemoryCatalogoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CatalogoServiceRegressionTest {

    private InMemoryCatalogoRepository repo;
    private CatalogoService service;

    @BeforeEach
    void setUp() {
        repo = new InMemoryCatalogoRepository();
        service = new CatalogoService(repo);
    }

    @Test
    void shouldInitializeBaseFieldsOnlyOnce() {
        service.initiateCampiBase();

        assertEquals(CampoBaseDefinito.values().length, service.getCampiBase().size());
        assertThrows(IllegalStateException.class, service::initiateCampiBase);
    }

    @Test
    void shouldCreateExtraBaseFieldsWithTheirDeclaredType() {
        service.addCampiBaseConExtra(List.of("Codice interno"), List.of(TipoDato.INTERO));

        Campo extra = service.getCampiBase().stream()
                .filter(c -> c.getNome().equals("Codice interno"))
                .findFirst()
                .orElseThrow();

        assertEquals(TipoDato.INTERO, extra.getTipoDato());
        assertTrue(extra.isObbligatorio());
    }

    @Test
    void shouldManageCommonFieldsWithoutCategories() {
        service.initiateCampiBase();
        service.addCampoComune("Descrizione", TipoDato.STRINGA, false);

        assertTrue(service.setObbligatorietaCampoComune("Descrizione", true));
        assertTrue(service.removeCampoComune("Descrizione"));
        assertTrue(service.getCampiComuni().isEmpty());
    }

    @Test
    void shouldCreateAndRemoveCategoriesWithSpecificFields() {
        service.initiateCampiBase();
        service.createCategoria("Sport");
        service.addCampoSpecifico("Sport", "Attrezzatura", TipoDato.STRINGA, true);

        assertEquals(1, service.getCategorie().size());
        assertEquals("Attrezzatura", service.getCategorie().get(0).getCampiSpecifici().get(0).getNome());
        assertTrue(service.removeCategoria("Sport"));
        assertTrue(service.getCategorie().isEmpty());
    }
}
