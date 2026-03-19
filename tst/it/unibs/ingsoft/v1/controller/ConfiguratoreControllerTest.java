package it.unibs.ingsoft.v1.controller;

import it.unibs.ingsoft.v1.domain.Categoria;
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
        // Fix base fields
        cs.fissareCampiBase(List.of("Titolo", "Data", "Luogo"));
        assertEquals(3, cs.getCampiBase().size());

        // Add common fields
        cs.addCampoComune("Note", false);
        assertEquals(1, cs.getCampiComuni().size());

        // Create category
        Categoria cat = cs.createCategoria("Sport");
        assertNotNull(cat);

        // Add specific field
        cs.addCampoSpecifico("Sport", "Certificato medico", true);
        assertEquals(1, cs.getCategoriaOrThrow("Sport").getCampiSpecifici().size());
    }

    @Test
    @DisplayName("Visualize categories shows all data")
    void visualize_showsAllData()
    {
        cs.fissareCampiBase(List.of("Titolo"));
        cs.addCampoComune("Note", false);
        cs.createCategoria("Arte");
        cs.createCategoria("Sport");

        List<Categoria> categorie = cs.getCategorie();
        assertEquals(2, categorie.size());
        assertEquals(1, cs.getCampiBase().size());
        assertEquals(1, cs.getCampiComuni().size());
    }

    @Test
    @DisplayName("Removing a category removes its specific fields too")
    void removeCategoria_removesSpecificFields()
    {
        cs.fissareCampiBase(List.of("Titolo"));
        cs.createCategoria("Sport");
        cs.addCampoSpecifico("Sport", "Cert", true);
        cs.removeCategoria("Sport");

        assertTrue(cs.getCategorie().isEmpty());
    }

    @Test
    @DisplayName("nomeEsiste returns true for base, common, and specific fields")
    void nomeEsiste_crossFieldTypes()
    {
        cs.fissareCampiBase(List.of("Titolo"));
        cs.addCampoComune("Note", false);
        cs.createCategoria("Sport");
        cs.addCampoSpecifico("Sport", "Cert", true);

        assertTrue(cs.nomeEsiste("titolo"));   // base (case-insensitive)
        assertTrue(cs.nomeEsiste("NOTE"));     // common
        assertTrue(cs.nomeEsiste("cert"));     // specific
        assertFalse(cs.nomeEsiste("Inesistente"));
    }
}
