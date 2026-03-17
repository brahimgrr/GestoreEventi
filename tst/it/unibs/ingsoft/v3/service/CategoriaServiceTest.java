package it.unibs.ingsoft.v3.service;

import it.unibs.ingsoft.v3.model.*;
import it.unibs.ingsoft.v3.persistence.AppData;
import it.unibs.ingsoft.v3.persistence.IPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – CategoriaService")
class CategoriaServiceTest
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

    @Test @DisplayName("Base fields auto-initialized")
    void baseFields() { assertEquals(CampoBaseDefinito.values().length, cs.getCampiBase().size()); }

    @Test @DisplayName("Create/remove category")
    void createRemove()
    {
        cs.createCategoria("Art");
        assertEquals(1, cs.getCategorie().size());
        assertTrue(cs.removeCategoria("Art"));
    }

    @Test @DisplayName("Add common field with TipoDato")
    void addCommon()
    {
        cs.addCampoComune("Note", TipoDato.STRINGA, false);
        assertEquals(1, cs.getCampiComuni().size());
    }
}
