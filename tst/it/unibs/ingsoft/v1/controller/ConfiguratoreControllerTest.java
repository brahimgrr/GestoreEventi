package it.unibs.ingsoft.v1.controller;

import it.unibs.ingsoft.v1.domain.Categoria;
import it.unibs.ingsoft.v1.domain.TipoDato;
import it.unibs.ingsoft.v1.application.CatalogoService;
import it.unibs.ingsoft.v1.persistence.api.ICategoriaRepository;
import it.unibs.ingsoft.v1.persistence.dto.CatalogoData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConfiguratoreController delegates all operations to CatalogoService.
 * We test the service-level operations that the controller orchestrates.
 */
@DisplayName("V1 – ConfiguratoreController (service integration)")
class ConfiguratoreControllerTest
{
    private CatalogoService cs;

    @BeforeEach
    void setUp()
    {
        CatalogoData data = new CatalogoData();
        ICategoriaRepository mockRepo = new ICategoriaRepository()
        {
            @Override public CatalogoData load()        { return data; }
            @Override public void save(CatalogoData d)  {}
        };
        cs = new CatalogoService(mockRepo, data);
    }

    @Test
    @DisplayName("Full workflow: fix base fields → add common → create category → add specific")
    void fullWorkflow()
    {
        // Fix base fields (8 predefined)
        cs.fissareCampiBase();
        assertEquals(8, cs.getCampiBase().size());

        // Add common fields
        cs.addCampoComune("Note", TipoDato.STRINGA, false);
        assertEquals(1, cs.getCampiComuni().size());

        // Create category
        Categoria cat = cs.createCategoria("Sport");
        assertNotNull(cat);

        // Add specific field
        cs.addCampoSpecifico("Sport", "Certificato medico", TipoDato.BOOLEANO, true);
        assertEquals(1, cs.getCategoriaOrThrow("Sport").getCampiSpecifici().size());
    }

    @Test
    @DisplayName("Full workflow with extra base fields")
    void fullWorkflow_withExtras()
    {
        cs.fissareCampiBaseConExtra(List.of("Campo Extra"));
        assertEquals(9, cs.getCampiBase().size());

        cs.createCategoria("Arte");
        cs.addCampoSpecifico("Arte", "Tecnica", TipoDato.STRINGA, false);
        assertEquals(1, cs.getCategoriaOrThrow("Arte").getCampiSpecifici().size());
    }

    @Test
    @DisplayName("Visualize categories shows all data")
    void visualize_showsAllData()
    {
        cs.fissareCampiBase();
        cs.addCampoComune("Note", TipoDato.STRINGA, false);
        cs.createCategoria("Arte");
        cs.createCategoria("Sport");

        List<Categoria> categorie = cs.getCategorie();
        assertEquals(2, categorie.size());
        assertEquals(8, cs.getCampiBase().size());
        assertEquals(1, cs.getCampiComuni().size());
    }

    @Test
    @DisplayName("Removing a category removes its specific fields too")
    void removeCategoria_removesSpecificFields()
    {
        cs.fissareCampiBase();
        cs.createCategoria("Sport");
        cs.addCampoSpecifico("Sport", "Cert", TipoDato.BOOLEANO, true);
        cs.removeCategoria("Sport");

        assertTrue(cs.getCategorie().isEmpty());
    }

    @Test
    @DisplayName("nomeEsiste returns true for base and common fields")
    void nomeEsiste_baseAndCommon()
    {
        cs.fissareCampiBase();
        cs.addCampoComune("Note", TipoDato.STRINGA, false);

        assertTrue(cs.nomeEsiste("titolo"));   // base (case-insensitive)
        assertTrue(cs.nomeEsiste("NOTE"));     // common
        assertFalse(cs.nomeEsiste("Inesistente"));
    }

    @Test
    @DisplayName("Same specific field name allowed in different categories")
    void specificFieldName_allowedAcrossCategories()
    {
        cs.fissareCampiBase();
        cs.createCategoria("Sport");
        cs.createCategoria("Arte");

        cs.addCampoSpecifico("Sport", "Certificato", TipoDato.BOOLEANO, true);
        // Same name in a different category should be allowed
        assertDoesNotThrow(
                () -> cs.addCampoSpecifico("Arte", "Certificato", TipoDato.STRINGA, false));
    }

    @Test
    @DisplayName("getCampiCondivisi returns base + common fields")
    void getCampiCondivisi_returnsBaseAndCommon()
    {
        cs.fissareCampiBase();
        cs.addCampoComune("Note", TipoDato.STRINGA, false);

        assertEquals(9, cs.getCampiCondivisi().size()); // 8 base + 1 common
    }
}
