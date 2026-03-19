package it.unibs.ingsoft.v2.application;

import it.unibs.ingsoft.v2.domain.*;
import it.unibs.ingsoft.v2.persistence.api.ICategoriaRepository;
import it.unibs.ingsoft.v2.persistence.dto.CatalogoData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – CampoService + CategoriaService")
class CategoriaServiceTest
{
    private CampoService     campo;
    private CategoriaService cs;

    @BeforeEach
    void setUp()
    {
        CatalogoData catalogo = new CatalogoData();
        ICategoriaRepository catRepo = new ICategoriaRepository()
        {
            @Override public CatalogoData load()              { return catalogo; }
            @Override public void save(CatalogoData d)        {}
        };
        campo = new CampoService(catRepo, catalogo);
        cs    = new CategoriaService(catRepo, catalogo, campo);
    }

    // ── CampoService – base fields ──────────────────────────────────────────

    @Test
    @DisplayName("Base fields are auto-initialized from CampoBaseDefinito")
    void baseFields_autoInitialized()
    {
        assertEquals(CampoBaseDefinito.values().length, campo.getCampiBase().size());
    }

    @Test
    @DisplayName("aggiungiCampiBaseExtra adds extra base fields and fixes them")
    void aggiungiCampiBaseExtra_valid()
    {
        int before = campo.getCampiBase().size();
        campo.aggiungiCampiBaseExtra(List.of("ExtraCampo"), List.of(TipoDato.STRINGA));
        assertEquals(before + 1, campo.getCampiBase().size());
        assertTrue(campo.isCampiBaseFissati());
    }

    @Test
    @DisplayName("aggiungiCampiBaseExtra twice throws IllegalStateException")
    void aggiungiCampiBaseExtra_twice_throws()
    {
        campo.fissaCampiBaseSenzaExtra();
        assertThrows(IllegalStateException.class,
                () -> campo.aggiungiCampiBaseExtra(List.of("X"), List.of(TipoDato.STRINGA)));
    }

    @Test
    @DisplayName("aggiungiCampiBaseExtra with fixed base name throws")
    void aggiungiCampiBaseExtra_fixedName_throws()
    {
        assertThrows(IllegalArgumentException.class,
                () -> campo.aggiungiCampiBaseExtra(List.of("Titolo"), List.of(TipoDato.STRINGA)));
    }

    @Test
    @DisplayName("addCampoComune adds common field with TipoDato")
    void addCampoComune_valid()
    {
        campo.addCampoComune("Note", TipoDato.STRINGA, false);
        assertEquals(1, campo.getCampiComuni().size());
    }

    @Test
    @DisplayName("addCampoComune with conflicting base field name throws")
    void addCampoComune_conflictsBase_throws()
    {
        assertThrows(IllegalArgumentException.class,
                () -> campo.addCampoComune("Titolo", TipoDato.STRINGA, false));
    }

    // ── CategoriaService ────────────────────────────────────────────────────

    @Test
    @DisplayName("createCategoria creates a category retrievable by name")
    void createCategoria_valid()
    {
        cs.createCategoria("Arte");
        assertEquals("Arte", cs.getCategoriaOrThrow("Arte").getNome());
    }

    @Test
    @DisplayName("addCampoSpecifico adds specific field to category")
    void addCampoSpecifico_valid()
    {
        cs.createCategoria("Sport");
        cs.addCampoSpecifico("Sport", "Cert", TipoDato.BOOLEANO, true);
        assertEquals(1, cs.getCategoriaOrThrow("Sport").getCampiSpecifici().size());
    }
}
