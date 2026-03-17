package it.unibs.ingsoft.v4.service;
import it.unibs.ingsoft.v4.model.*; import it.unibs.ingsoft.v4.persistence.AppData; import it.unibs.ingsoft.v4.persistence.IPersistenceService;
import org.junit.jupiter.api.BeforeEach; import org.junit.jupiter.api.Test; import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("V4 – CategoriaService") class CategoriaServiceTest { private CategoriaService cs;
@BeforeEach void setUp() { AppData d = new AppData(); IPersistenceService db = new IPersistenceService() { @Override public AppData loadOrCreate() { return d; } @Override public void save(AppData x) {} }; cs = new CategoriaService(db, d); }
@Test @DisplayName("Base fields auto-init") void base() { assertEquals(CampoBaseDefinito.values().length, cs.getCampiBase().size()); }
@Test @DisplayName("Create category") void create() { cs.createCategoria("S"); assertEquals(1, cs.getCategorie().size()); }
@Test @DisplayName("Add common field") void common() { cs.addCampoComune("Note", TipoDato.STRINGA, false); assertEquals(1, cs.getCampiComuni().size()); } }
