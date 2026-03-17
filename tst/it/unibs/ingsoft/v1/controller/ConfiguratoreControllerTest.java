package it.unibs.ingsoft.v1.controller;

import it.unibs.ingsoft.v1.model.Campo;
import it.unibs.ingsoft.v1.model.Categoria;
import it.unibs.ingsoft.v1.persistence.AppData;
import it.unibs.ingsoft.v1.persistence.IPersistenceService;
import it.unibs.ingsoft.v1.service.CategoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConfiguratoreController delegates all operations to CategoriaService.
 * We test the service-level operations that the controller orchestrates.
 */
@DisplayName("V1 – ConfiguratoreController (service integration)")
class ConfiguratoreControllerTest
{
    private CategoriaService cs;

    @BeforeEach
    void setUp()
    {
        AppData data = new AppData();
        IPersistenceService mockDb = new IPersistenceService()
        {
            @Override public AppData loadOrCreate() { return data; }
            @Override public void save(AppData d) {}
        };
        cs = new CategoriaService(mockDb, data);
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
        cs.createCategoria("Sport");
        cs.addCampoSpecifico("Sport", "Cert", true);
        cs.removeCategoria("Sport");

        assertTrue(cs.getCategorie().isEmpty());
    }
}
