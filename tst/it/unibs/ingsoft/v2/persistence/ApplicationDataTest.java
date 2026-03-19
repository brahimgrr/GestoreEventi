package it.unibs.ingsoft.v2.persistence;

import it.unibs.ingsoft.v2.domain.*;
import it.unibs.ingsoft.v2.persistence.dto.CatalogoData;
import it.unibs.ingsoft.v2.persistence.dto.PropostaData;
import it.unibs.ingsoft.v2.persistence.dto.UtenteData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – CatalogoData, UtenteData, PropostaData")
class ApplicationDataTest
{
    private CatalogoData catalogo;
    private UtenteData   utenti;
    private PropostaData proposte;

    @BeforeEach
    void setUp()
    {
        catalogo  = new CatalogoData();
        utenti    = new UtenteData();
        proposte  = new PropostaData();
    }

    // ── UtenteData ──────────────────────────────────────────────────────────

    @Test @DisplayName("UtenteData: addConfiguratore then getConfiguratori")
    void utenteData_addGet()
    {
        utenti.addConfiguratore("mario", "pass1234");
        assertEquals(1, utenti.getConfiguratori().size());
        assertEquals("pass1234", utenti.getConfiguratori().get("mario"));
    }

    // ── CatalogoData – campi base ───────────────────────────────────────────

    @Test @DisplayName("CatalogoData: addCampoBase then getCampiBase")
    void catalogoData_campiBase_addGet()
    {
        catalogo.addCampoBase(new Campo("T", TipoCampo.BASE, TipoDato.STRINGA, true));
        assertEquals(1, catalogo.getCampiBase().size());
    }

    // ── CatalogoData – campi comuni ─────────────────────────────────────────

    @Test @DisplayName("CatalogoData: addCampoComune then removeCampoComune")
    void catalogoData_campiComuni_addRemove()
    {
        catalogo.addCampoComune(new Campo("Note", TipoCampo.COMUNE, TipoDato.STRINGA, false));
        assertEquals(1, catalogo.getCampiComuni().size());
        assertTrue(catalogo.removeCampoComune("Note"));
        assertTrue(catalogo.getCampiComuni().isEmpty());
    }

    // ── CatalogoData – categorie ────────────────────────────────────────────

    @Test @DisplayName("CatalogoData: addCategoria, findCategoria, removeCategoria")
    void catalogoData_categorie_addFindRemove()
    {
        catalogo.addCategoria(new Categoria("Sport"));
        assertTrue(catalogo.findCategoria("sport").isPresent());
        assertTrue(catalogo.removeCategoria("Sport"));
        assertTrue(catalogo.findCategoria("Sport").isEmpty());
    }

    // ── PropostaData ────────────────────────────────────────────────────────

    @Test @DisplayName("PropostaData: addProposta then getProposte")
    void propostaData_addGet()
    {
        Proposta p = new Proposta(new Categoria("Sport"));
        proposte.addProposta(p);
        assertEquals(1, proposte.getProposte().size());
    }

    // ── CatalogoData – campiBaseFissati flag ────────────────────────────────

    @Test @DisplayName("CatalogoData: campiBaseFissati is false initially, settable to true")
    void catalogoData_campiBaseFissati()
    {
        assertFalse(catalogo.isCampiBaseFissati());
        catalogo.setCampiBaseFissati(true);
        assertTrue(catalogo.isCampiBaseFissati());
    }
}
