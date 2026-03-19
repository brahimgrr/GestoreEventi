package it.unibs.ingsoft.v1.application;

import it.unibs.ingsoft.v1.domain.Categoria;
import it.unibs.ingsoft.v1.domain.TipoDato;
import it.unibs.ingsoft.v1.persistence.api.ICategoriaRepository;
import it.unibs.ingsoft.v1.persistence.dto.CatalogoData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V1 – CatalogoService")
class CategoriaServiceTest
{
    private CatalogoData catalogo;
    private CatalogoService service;
    private int saveCount;

    @BeforeEach
    void setUp()
    {
        catalogo = new CatalogoData();
        saveCount = 0;
        ICategoriaRepository mockRepo = new ICategoriaRepository()
        {
            @Override public CatalogoData load() { return catalogo; }
            @Override public void save(CatalogoData d) { saveCount++; }
        };
        service = new CatalogoService(mockRepo, catalogo);
    }

    // ───────────────────── Campi Base ─────────────────────

    @Test
    @DisplayName("fissareCampiBase populates 8 predefined fields")
    void fissareCampiBase_populatesPredefinedFields()
    {
        service.fissareCampiBase();

        assertEquals(8, service.getCampiBase().size());
        assertTrue(catalogo.isCampiBaseFissati());
        assertTrue(saveCount > 0);
    }

    @Test
    @DisplayName("fissareCampiBase includes Titolo among predefined fields")
    void fissareCampiBase_includesTitolo()
    {
        service.fissareCampiBase();
        assertTrue(service.getCampiBase().stream()
                .anyMatch(c -> c.getNome().equals("Titolo")));
    }

    @Test
    @DisplayName("fissareCampiBase twice throws IllegalStateException")
    void fissareCampiBase_alreadyFixed_throwsException()
    {
        service.fissareCampiBase();
        assertThrows(IllegalStateException.class,
                () -> service.fissareCampiBase());
    }

    @Test
    @DisplayName("fissareCampiBaseConExtra adds extra fields after predefined")
    void fissareCampiBaseConExtra_addsExtras()
    {
        service.fissareCampiBaseConExtra(List.of("Campo Extra"));
        assertEquals(9, service.getCampiBase().size());
    }

    @Test
    @DisplayName("fissareCampiBaseConExtra with duplicate name throws")
    void fissareCampiBaseConExtra_duplicateName_throwsException()
    {
        assertThrows(IllegalArgumentException.class,
                () -> service.fissareCampiBaseConExtra(List.of("Titolo")));
    }

    // ───────────────────── Campi Comuni ─────────────────────

    @Test
    @DisplayName("addCampoComune adds a common field with TipoDato")
    void addCampoComune_valid_added()
    {
        service.addCampoComune("Note", TipoDato.STRINGA, false);
        assertEquals(1, service.getCampiComuni().size());
        assertEquals("Note", service.getCampiComuni().get(0).getNome());
        assertEquals(TipoDato.STRINGA, service.getCampiComuni().get(0).getTipoDato());
    }

    @Test
    @DisplayName("addCampoComune with name conflicting base field throws")
    void addCampoComune_conflictsWithBase_throwsException()
    {
        service.fissareCampiBase();
        assertThrows(IllegalArgumentException.class,
                () -> service.addCampoComune("Titolo", TipoDato.STRINGA, false));
    }

    @Test
    @DisplayName("addCampoComune with duplicate common name throws")
    void addCampoComune_duplicateCommon_throwsException()
    {
        service.addCampoComune("Note", TipoDato.STRINGA, false);
        assertThrows(IllegalArgumentException.class,
                () -> service.addCampoComune("note", TipoDato.STRINGA, true));
    }

    @Test
    @DisplayName("removeCampoComune removes existing field")
    void removeCampoComune_existing_removed()
    {
        service.addCampoComune("Note", TipoDato.STRINGA, false);
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
        service.addCampoComune("Note", TipoDato.STRINGA, false);
        assertTrue(service.setObbligatorietaCampoComune("Note", true));
        assertTrue(service.getCampiComuni().get(0).isObbligatorio());
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

    // ───────────────────── Campi Specifici ─────────────────────

    @Test
    @DisplayName("addCampoSpecifico adds field to category")
    void addCampoSpecifico_valid_added()
    {
        service.createCategoria("Sport");
        service.addCampoSpecifico("Sport", "Certificato", TipoDato.BOOLEANO, true);

        Categoria cat = service.getCategoriaOrThrow("Sport");
        assertEquals(1, cat.getCampiSpecifici().size());
        assertEquals("Certificato", cat.getCampiSpecifici().get(0).getNome());
        assertEquals(TipoDato.BOOLEANO, cat.getCampiSpecifici().get(0).getTipoDato());
    }

    @Test
    @DisplayName("addCampoSpecifico with name conflicting base field throws")
    void addCampoSpecifico_conflictsWithBase_throwsException()
    {
        service.fissareCampiBase();
        service.createCategoria("Sport");
        assertThrows(IllegalArgumentException.class,
                () -> service.addCampoSpecifico("Sport", "Titolo", TipoDato.STRINGA, true));
    }

    @Test
    @DisplayName("Same specific field name allowed in different categories")
    void addCampoSpecifico_sameNameDifferentCategories_allowed()
    {
        service.createCategoria("Sport");
        service.createCategoria("Arte");
        service.addCampoSpecifico("Sport", "Certificato", TipoDato.BOOLEANO, true);
        // Should NOT throw — per-category uniqueness only
        assertDoesNotThrow(
                () -> service.addCampoSpecifico("Arte", "Certificato", TipoDato.STRINGA, false));
    }

    // ───────────────────── Persistence ─────────────────────

    @Test
    @DisplayName("Each mutation triggers save")
    void mutation_triggersSave()
    {
        int before = saveCount;
        service.createCategoria("Sport");
        assertTrue(saveCount > before);
    }
}
