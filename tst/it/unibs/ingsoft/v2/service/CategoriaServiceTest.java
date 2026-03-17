package it.unibs.ingsoft.v2.service;

import it.unibs.ingsoft.v2.model.*;
import it.unibs.ingsoft.v2.persistence.AppData;
import it.unibs.ingsoft.v2.persistence.IPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – CategoriaService")
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

    @Test
    @DisplayName("Base fields are auto-initialized from CampoBaseDefinito")
    void baseFields_autoInitialized()
    {
        assertEquals(CampoBaseDefinito.values().length, cs.getCampiBase().size());
    }

    @Test
    @DisplayName("aggiungiCampiBaseExtra adds extra base fields")
    void aggiungiCampiBaseExtra_valid()
    {
        int before = cs.getCampiBase().size();
        cs.aggiungiCampiBaseExtra(List.of("ExtraCampo"), List.of(TipoDato.STRINGA));
        assertEquals(before + 1, cs.getCampiBase().size());
        assertTrue(cs.isCampiBaseFissati());
    }

    @Test
    @DisplayName("aggiungiCampiBaseExtra twice throws IllegalStateException")
    void aggiungiCampiBaseExtra_twice_throws()
    {
        cs.fissaCampiBaseSenzaExtra();
        assertThrows(IllegalStateException.class,
                () -> cs.aggiungiCampiBaseExtra(List.of("X"), List.of(TipoDato.STRINGA)));
    }

    @Test
    @DisplayName("aggiungiCampiBaseExtra with fixed base name throws")
    void aggiungiCampiBaseExtra_fixedName_throws()
    {
        assertThrows(IllegalArgumentException.class,
                () -> cs.aggiungiCampiBaseExtra(List.of("Titolo"), List.of(TipoDato.STRINGA)));
    }

    @Test
    @DisplayName("addCampoComune adds common field with TipoDato")
    void addCampoComune_valid()
    {
        cs.addCampoComune("Note", TipoDato.STRINGA, false);
        assertEquals(1, cs.getCampiComuni().size());
    }

    @Test
    @DisplayName("addCampoComune with conflicting base field name throws")
    void addCampoComune_conflictsBase_throws()
    {
        assertThrows(IllegalArgumentException.class,
                () -> cs.addCampoComune("Titolo", TipoDato.STRINGA, false));
    }

    @Test
    @DisplayName("createCategoria works correctly")
    void createCategoria_valid()
    {
        Categoria cat = cs.createCategoria("Arte");
        assertEquals("Arte", cat.getNome());
    }

    @Test
    @DisplayName("addCampoSpecifico with TipoDato")
    void addCampoSpecifico_valid()
    {
        cs.createCategoria("Sport");
        cs.addCampoSpecifico("Sport", "Cert", TipoDato.BOOLEANO, true);
        assertEquals(1, cs.getCategoriaOrThrow("Sport").getCampiSpecifici().size());
    }
}
