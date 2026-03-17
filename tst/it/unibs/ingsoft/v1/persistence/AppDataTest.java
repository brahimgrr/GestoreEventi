package it.unibs.ingsoft.v1.persistence;

import it.unibs.ingsoft.v1.model.Campo;
import it.unibs.ingsoft.v1.model.Categoria;
import it.unibs.ingsoft.v1.model.TipoCampo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V1 – AppData")
class AppDataTest
{
    private AppData data;

    @BeforeEach
    void setUp()
    {
        data = new AppData();
    }

    // ───────────────────── Configuratori ─────────────────────

    @Test
    @DisplayName("addConfiguratore and getConfiguratori")
    void configuratori_addAndGet()
    {
        data.addConfiguratore("mario", "pass1234");
        assertEquals(1, data.getConfiguratori().size());
        assertEquals("pass1234", data.getConfiguratori().get("mario"));
    }

    @Test
    @DisplayName("getConfiguratori returns unmodifiable map")
    void configuratori_unmodifiable()
    {
        assertThrows(UnsupportedOperationException.class,
                () -> data.getConfiguratori().put("x", "y"));
    }

    // ───────────────────── Campi Base ─────────────────────

    @Test
    @DisplayName("addCampoBase and getCampiBase")
    void campiBase_addAndGet()
    {
        Campo c = new Campo("Titolo", TipoCampo.BASE, true);
        data.addCampoBase(c);
        assertEquals(1, data.getCampiBase().size());
        assertEquals("Titolo", data.getCampiBase().get(0).getNome());
    }

    @Test
    @DisplayName("campiBaseFissati flag defaults to false")
    void campiBaseFissati_defaultFalse()
    {
        assertFalse(data.isCampiBaseFissati());
    }

    @Test
    @DisplayName("setCampiBaseFissati sets the flag")
    void campiBaseFissati_set()
    {
        data.setCampiBaseFissati(true);
        assertTrue(data.isCampiBaseFissati());
    }

    @Test
    @DisplayName("clearCampiBase clears all base fields")
    void campiBase_clear()
    {
        data.addCampoBase(new Campo("Titolo", TipoCampo.BASE, true));
        data.clearCampiBase();
        assertTrue(data.getCampiBase().isEmpty());
    }

    @Test
    @DisplayName("getCampiBase returns unmodifiable list")
    void campiBase_unmodifiable()
    {
        assertThrows(UnsupportedOperationException.class,
                () -> data.getCampiBase().add(new Campo("x", TipoCampo.BASE, true)));
    }

    // ───────────────────── Campi Comuni ─────────────────────

    @Test
    @DisplayName("addCampoComune adds and sorts alphabetically")
    void campiComuni_addAndSort()
    {
        data.addCampoComune(new Campo("Zzz", TipoCampo.COMUNE, false));
        data.addCampoComune(new Campo("Aaa", TipoCampo.COMUNE, false));
        assertEquals("Aaa", data.getCampiComuni().get(0).getNome());
        assertEquals("Zzz", data.getCampiComuni().get(1).getNome());
    }

    @Test
    @DisplayName("removeCampoComune is case-insensitive")
    void campiComuni_removeCaseInsensitive()
    {
        data.addCampoComune(new Campo("Note", TipoCampo.COMUNE, false));
        assertTrue(data.removeCampoComune("NOTE"));
        assertTrue(data.getCampiComuni().isEmpty());
    }

    // ───────────────────── Categorie ─────────────────────

    @Test
    @DisplayName("addCategoria adds and sorts alphabetically")
    void categorie_addAndSort()
    {
        data.addCategoria(new Categoria("Sport"));
        data.addCategoria(new Categoria("Arte"));
        assertEquals("Arte", data.getCategorie().get(0).getNome());
        assertEquals("Sport", data.getCategorie().get(1).getNome());
    }

    @Test
    @DisplayName("removeCategoria is case-insensitive")
    void categorie_removeCaseInsensitive()
    {
        data.addCategoria(new Categoria("Sport"));
        assertTrue(data.removeCategoria("SPORT"));
        assertTrue(data.getCategorie().isEmpty());
    }

    @Test
    @DisplayName("findCategoria returns Optional with existing category")
    void categorie_findExisting()
    {
        data.addCategoria(new Categoria("Sport"));
        assertTrue(data.findCategoria("sport").isPresent());
    }

    @Test
    @DisplayName("findCategoria returns empty Optional for non-existing")
    void categorie_findNonExisting()
    {
        assertFalse(data.findCategoria("Inesistente").isPresent());
    }

    @Test
    @DisplayName("getCategorie returns unmodifiable list")
    void categorie_unmodifiable()
    {
        assertThrows(UnsupportedOperationException.class,
                () -> data.getCategorie().add(new Categoria("x")));
    }
}
