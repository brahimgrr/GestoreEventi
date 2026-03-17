package it.unibs.ingsoft.v1.service;

import it.unibs.ingsoft.v1.model.Campo;
import it.unibs.ingsoft.v1.model.Categoria;
import it.unibs.ingsoft.v1.model.TipoCampo;
import it.unibs.ingsoft.v1.persistence.AppData;
import it.unibs.ingsoft.v1.persistence.IPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V1 – CategoriaService")
class CategoriaServiceTest
{
    private AppData data;
    private CategoriaService service;
    private int saveCount;

    @BeforeEach
    void setUp()
    {
        data = new AppData();
        saveCount = 0;
        IPersistenceService mockDb = new IPersistenceService()
        {
            @Override public AppData loadOrCreate() { return data; }
            @Override public void save(AppData d) { saveCount++; }
        };
        service = new CategoriaService(mockDb, data);
    }

    // ───────────────────── Campi Base ─────────────────────

    @Test
    @DisplayName("fissareCampiBase with valid list fixes base fields")
    void fissareCampiBase_validList_fixesFields()
    {
        service.fissareCampiBase(Arrays.asList("Titolo", "Luogo", "Data"));

        assertEquals(3, service.getCampiBase().size());
        assertTrue(data.isCampiBaseFissati());
        assertTrue(saveCount > 0);
    }

    @Test
    @DisplayName("fissareCampiBase twice throws IllegalStateException")
    void fissareCampiBase_alreadyFixed_throwsException()
    {
        service.fissareCampiBase(Arrays.asList("Titolo", "Luogo"));
        assertThrows(IllegalStateException.class,
                () -> service.fissareCampiBase(List.of("Altro")));
    }

    @Test
    @DisplayName("fissareCampiBase with empty list throws IllegalArgumentException")
    void fissareCampiBase_emptyList_throwsException()
    {
        assertThrows(IllegalArgumentException.class,
                () -> service.fissareCampiBase(Collections.emptyList()));
    }

    @Test
    @DisplayName("fissareCampiBase with duplicate names throws IllegalArgumentException")
    void fissareCampiBase_duplicateNames_throwsException()
    {
        assertThrows(IllegalArgumentException.class,
                () -> service.fissareCampiBase(Arrays.asList("Titolo", "titolo")));
    }

    @Test
    @DisplayName("fissareCampiBase skips blank entries")
    void fissareCampiBase_blankEntries_skipped()
    {
        service.fissareCampiBase(Arrays.asList("Titolo", "   ", "", "Luogo"));
        assertEquals(2, service.getCampiBase().size());
    }

    @Test
    @DisplayName("fissareCampiBase creates only mandatory fields")
    void fissareCampiBase_allMandatory()
    {
        service.fissareCampiBase(List.of("Titolo"));
        assertTrue(service.getCampiBase().get(0).isObbligatorio());
    }

    // ───────────────────── Campi Comuni ─────────────────────

    @Test
    @DisplayName("addCampoComune adds a common field")
    void addCampoComune_valid_added()
    {
        service.addCampoComune("Note", false);
        assertEquals(1, service.getCampiComuni().size());
        assertEquals("Note", service.getCampiComuni().get(0).getNome());
    }

    @Test
    @DisplayName("addCampoComune with name conflicting base field throws")
    void addCampoComune_conflictsWithBase_throwsException()
    {
        service.fissareCampiBase(List.of("Titolo"));
        assertThrows(IllegalArgumentException.class,
                () -> service.addCampoComune("Titolo", false));
    }

    @Test
    @DisplayName("addCampoComune with duplicate common name throws")
    void addCampoComune_duplicateCommon_throwsException()
    {
        service.addCampoComune("Note", false);
        assertThrows(IllegalArgumentException.class,
                () -> service.addCampoComune("note", true));
    }

    @Test
    @DisplayName("removeCampoComune removes existing field")
    void removeCampoComune_existing_removed()
    {
        service.addCampoComune("Note", false);
        assertTrue(service.removeCampoComune("Note"));
        assertTrue(service.getCampiComuni().isEmpty());
    }

    @Test
    @DisplayName("removeCampoComune with non-existing name returns false")
    void removeCampoComune_nonExisting_returnsFalse()
    {
        assertFalse(service.removeCampoComune("Inesistente"));
    }

    @Test
    @DisplayName("setObbligatorietaCampoComune updates flag")
    void setObbligatorietaCampoComune_existing_updates()
    {
        service.addCampoComune("Note", false);
        assertTrue(service.setObbligatorietaCampoComune("Note", true));
        assertTrue(service.getCampiComuni().get(0).isObbligatorio());
    }

    @Test
    @DisplayName("setObbligatorietaCampoComune with non-existing name returns false")
    void setObbligatorietaCampoComune_nonExisting_returnsFalse()
    {
        assertFalse(service.setObbligatorietaCampoComune("Inesistente", true));
    }

    // ───────────────────── Categorie ─────────────────────

    @Test
    @DisplayName("createCategoria creates a new category")
    void createCategoria_valid_created()
    {
        Categoria cat = service.createCategoria("Sport");
        assertEquals("Sport", cat.getNome());
        assertEquals(1, service.getCategorie().size());
    }

    @Test
    @DisplayName("createCategoria with duplicate name throws")
    void createCategoria_duplicate_throwsException()
    {
        service.createCategoria("Sport");
        assertThrows(IllegalArgumentException.class,
                () -> service.createCategoria("sport"));
    }

    @Test
    @DisplayName("removeCategoria removes existing category")
    void removeCategoria_existing_removed()
    {
        service.createCategoria("Sport");
        assertTrue(service.removeCategoria("Sport"));
        assertTrue(service.getCategorie().isEmpty());
    }

    @Test
    @DisplayName("removeCategoria with non-existing name returns false")
    void removeCategoria_nonExisting_returnsFalse()
    {
        assertFalse(service.removeCategoria("Inesistente"));
    }

    @Test
    @DisplayName("getCategoriaOrThrow with existing category returns it")
    void getCategoriaOrThrow_existing_returns()
    {
        service.createCategoria("Sport");
        assertNotNull(service.getCategoriaOrThrow("Sport"));
    }

    @Test
    @DisplayName("getCategoriaOrThrow with non-existing throws exception")
    void getCategoriaOrThrow_nonExisting_throwsException()
    {
        assertThrows(IllegalArgumentException.class,
                () -> service.getCategoriaOrThrow("Inesistente"));
    }

    // ───────────────────── Campi Specifici ─────────────────────

    @Test
    @DisplayName("addCampoSpecifico adds field to the correct category")
    void addCampoSpecifico_valid_added()
    {
        service.createCategoria("Sport");
        service.addCampoSpecifico("Sport", "Certificato", true);

        Categoria cat = service.getCategoriaOrThrow("Sport");
        assertEquals(1, cat.getCampiSpecifici().size());
        assertEquals("Certificato", cat.getCampiSpecifici().get(0).getNome());
    }

    @Test
    @DisplayName("addCampoSpecifico with name conflicting base field throws")
    void addCampoSpecifico_conflictsWithBase_throwsException()
    {
        service.fissareCampiBase(List.of("Titolo"));
        service.createCategoria("Sport");
        assertThrows(IllegalArgumentException.class,
                () -> service.addCampoSpecifico("Sport", "Titolo", true));
    }

    @Test
    @DisplayName("addCampoSpecifico with name conflicting common field throws")
    void addCampoSpecifico_conflictsWithCommon_throwsException()
    {
        service.addCampoComune("Note", false);
        service.createCategoria("Sport");
        assertThrows(IllegalArgumentException.class,
                () -> service.addCampoSpecifico("Sport", "Note", true));
    }

    @Test
    @DisplayName("removeCampoSpecifico removes existing field")
    void removeCampoSpecifico_existing_removed()
    {
        service.createCategoria("Sport");
        service.addCampoSpecifico("Sport", "Cert", true);
        assertTrue(service.removeCampoSpecifico("Sport", "Cert"));
    }

    @Test
    @DisplayName("setObbligatorietaCampoSpecifico updates flag")
    void setObbligatorietaCampoSpecifico_existing_updates()
    {
        service.createCategoria("Sport");
        service.addCampoSpecifico("Sport", "Cert", true);
        assertTrue(service.setObbligatorietaCampoSpecifico("Sport", "Cert", false));
    }

    // ───────────────────── Persistence ─────────────────────

    @Test
    @DisplayName("Each mutation triggers db.save")
    void mutation_triggersSave()
    {
        int before = saveCount;
        service.createCategoria("Sport");
        assertTrue(saveCount > before);
    }
}
