package it.unibs.ingsoft.v2.persistence;

import it.unibs.ingsoft.v2.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – AppData")
class AppDataTest
{
    private AppData data;

    @BeforeEach
    void setUp() { data = new AppData(); }

    @Test @DisplayName("Configuratori add/get")
    void configuratori() { data.addConfiguratore("m", "p"); assertEquals(1, data.getConfiguratori().size()); }

    @Test @DisplayName("Campi base add/get")
    void campiBase()
    {
        data.addCampoBase(new Campo("T", TipoCampo.BASE, TipoDato.STRINGA, true));
        assertEquals(1, data.getCampiBase().size());
    }

    @Test @DisplayName("Campi comuni add/remove sorted")
    void campiComuni()
    {
        data.addCampoComune(new Campo("Zzz", TipoCampo.COMUNE, TipoDato.STRINGA, false));
        data.addCampoComune(new Campo("Aaa", TipoCampo.COMUNE, TipoDato.STRINGA, false));
        assertEquals("Aaa", data.getCampiComuni().get(0).getNome());
        assertTrue(data.removeCampoComune("Zzz"));
    }

    @Test @DisplayName("Categorie add/find/remove")
    void categorie()
    {
        data.addCategoria(new Categoria("Sport"));
        assertTrue(data.findCategoria("sport").isPresent());
        assertTrue(data.removeCategoria("Sport"));
    }

    @Test @DisplayName("Proposte add/get")
    void proposte()
    {
        Proposta p = new Proposta(new Categoria("Sport"));
        data.addProposta(p);
        assertEquals(1, data.getProposte().size());
    }
}
