package it.unibs.ingsoft.v1.persistence;

import it.unibs.ingsoft.v1.domain.Campo;
import it.unibs.ingsoft.v1.domain.Categoria;
import it.unibs.ingsoft.v1.domain.TipoCampo;
import it.unibs.ingsoft.v1.persistence.dto.CatalogoData;
import it.unibs.ingsoft.v1.persistence.dto.UtenteData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V1 – CatalogoData / UtenteData (DTOs)")
class ApplicationDataTest
{
    private CatalogoData catalogo;
    private UtenteData   utenti;

    @BeforeEach
    void setUp()
    {
        catalogo = new CatalogoData();
        utenti   = new UtenteData();
    }

    // ───────────────────── UtenteData ─────────────────────

    @Test
    @DisplayName("addConfiguratore and getConfiguratori")
    void configuratori_addAndGet()
    {
        utenti.addConfiguratore("mario", "pass1234");
        assertEquals(1, utenti.getConfiguratori().size());
        assertEquals("pass1234", utenti.getConfiguratori().get("mario"));
    }

    @Test
    @DisplayName("getConfiguratori returns unmodifiable map")
    void configuratori_unmodifiable()
    {
        assertThrows(UnsupportedOperationException.class,
                () -> utenti.getConfiguratori().put("x", "y"));
    }

    // ───────────────────── CatalogoData — Campi Base ─────────────────────

    @Test
    @DisplayName("addCampoBase and getCampiBase")
    void campiBase_addAndGet()
    {
        Campo c = new Campo("Titolo", TipoCampo.BASE, true);
        catalogo.addCampoBase(c);
        assertEquals(1, catalogo.getCampiBase().size());
        assertEquals("Titolo", catalogo.getCampiBase().get(0).getNome());
    }

    @Test
    @DisplayName("isCampiBaseFissati defaults to false")
    void campiBaseFissati_defaultFalse()
    {
        assertFalse(catalogo.isCampiBaseFissati());
    }

    @Test
    @DisplayName("markCampiBaseFissati sets the flag")
    void campiBaseFissati_mark()
    {
        catalogo.markCampiBaseFissati();
        assertTrue(catalogo.isCampiBaseFissati());
    }

    @Test
    @DisplayName("addCampoBase throws after markCampiBaseFissati")
    void campiBase_addAfterFixed_throws()
    {
        catalogo.markCampiBaseFissati();
        assertThrows(IllegalStateException.class,
                () -> catalogo.addCampoBase(new Campo("X", TipoCampo.BASE, true)));
    }

    @Test
    @DisplayName("clearCampiBase clears all base fields")
    void campiBase_clear()
    {
        catalogo.addCampoBase(new Campo("Titolo", TipoCampo.BASE, true));
        catalogo.clearCampiBase();
        assertTrue(catalogo.getCampiBase().isEmpty());
    }

    @Test
    @DisplayName("getCampiBase returns unmodifiable list")
    void campiBase_unmodifiable()
    {
        assertThrows(UnsupportedOperationException.class,
                () -> catalogo.getCampiBase().add(new Campo("x", TipoCampo.BASE, true)));
    }

    // ───────────────────── CatalogoData — Campi Comuni ─────────────────────

    @Test
    @DisplayName("addCampoComune and getCampiComuni")
    void campiComuni_addAndGet()
    {
        catalogo.addCampoComune(new Campo("Note", TipoCampo.COMUNE, false));
        assertEquals(1, catalogo.getCampiComuni().size());
    }

    @Test
    @DisplayName("removeCampoComune is case-insensitive")
    void campiComuni_removeCaseInsensitive()
    {
        catalogo.addCampoComune(new Campo("Note", TipoCampo.COMUNE, false));
        assertTrue(catalogo.removeCampoComune("NOTE"));
        assertTrue(catalogo.getCampiComuni().isEmpty());
    }

    // ───────────────────── CatalogoData — Categorie ─────────────────────

    @Test
    @DisplayName("addCategoria and getCategorie")
    void categorie_addAndGet()
    {
        catalogo.addCategoria(new Categoria("Sport"));
        catalogo.addCategoria(new Categoria("Arte"));
        assertEquals(2, catalogo.getCategorie().size());
    }

    @Test
    @DisplayName("removeCategoria is case-insensitive")
    void categorie_removeCaseInsensitive()
    {
        catalogo.addCategoria(new Categoria("Sport"));
        assertTrue(catalogo.removeCategoria("SPORT"));
        assertTrue(catalogo.getCategorie().isEmpty());
    }

    @Test
    @DisplayName("getCategorie returns unmodifiable list")
    void categorie_unmodifiable()
    {
        assertThrows(UnsupportedOperationException.class,
                () -> catalogo.getCategorie().add(new Categoria("x")));
    }
}
