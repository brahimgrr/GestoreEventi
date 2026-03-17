package it.unibs.ingsoft.v5.service;
import it.unibs.ingsoft.v5.model.Fruitore; import it.unibs.ingsoft.v5.persistence.AppData; import it.unibs.ingsoft.v5.persistence.IPersistenceService;
import org.junit.jupiter.api.BeforeEach; import org.junit.jupiter.api.Test; import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("V5 – FruitoreService") class FruitoreServiceTest { private FruitoreService fs;
@BeforeEach void setUp() { AppData d = new AppData(); IPersistenceService db = new IPersistenceService() { @Override public AppData loadOrCreate() { return d; } @Override public void save(AppData x) {} }; NotificaService ns = new NotificaService(db, d); fs = new FruitoreService(db, d, ns); }
@Test @DisplayName("Register fruitore") void r() { Fruitore f = fs.registraFruitore("user1", "pass1234"); assertEquals("user1", f.getUsername()); }
@Test @DisplayName("Login fruitore") void l() { fs.registraFruitore("user1", "pass1234"); assertNotNull(fs.login("user1", "pass1234")); }
@Test @DisplayName("Duplicate throws") void d1() { fs.registraFruitore("user1", "pass1234"); assertThrows(IllegalArgumentException.class, () -> fs.registraFruitore("user1", "x")); } }
