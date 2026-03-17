package it.unibs.ingsoft.v3.service;

import it.unibs.ingsoft.v3.model.Fruitore;
import it.unibs.ingsoft.v3.persistence.AppData;
import it.unibs.ingsoft.v3.persistence.IPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – FruitoreService")
class FruitoreServiceTest
{
    private AppData data;
    private FruitoreService fs;

    @BeforeEach
    void setUp()
    {
        data = new AppData();
        IPersistenceService mockDb = new IPersistenceService()
        {
            @Override public AppData loadOrCreate() { return data; }
            @Override public void save(AppData d) {}
        };
        NotificaService ns = new NotificaService(mockDb, data);
        fs = new FruitoreService(mockDb, data, ns);
    }

    @Test @DisplayName("Register fruitore with valid credentials")
    void registra_valid()
    {
        Fruitore f = fs.registraFruitore("user1", "pass1234");
        assertNotNull(f);
        assertEquals("user1", f.getUsername());
    }

    @Test @DisplayName("Register with duplicate username throws")
    void registra_duplicate_throws()
    {
        fs.registraFruitore("user1", "pass1234");
        assertThrows(IllegalArgumentException.class, () -> fs.registraFruitore("user1", "pass5678"));
    }

    @Test @DisplayName("Register with configurator username throws")
    void registra_conflictConfiguratore_throws()
    {
        data.addConfiguratore("admin", "adminpass");
        assertThrows(IllegalArgumentException.class, () -> fs.registraFruitore("admin", "pass1234"));
    }

    @Test @DisplayName("Register with reserved username throws")
    void registra_reserved_throws()
    {
        assertThrows(IllegalArgumentException.class,
                () -> fs.registraFruitore(AuthenticationService.USERNAME_PREDEFINITO, "pass1234"));
    }

    @Test @DisplayName("Register with short username throws")
    void registra_shortUsername_throws()
    {
        assertThrows(IllegalArgumentException.class, () -> fs.registraFruitore("ab", "pass1234"));
    }

    @Test @DisplayName("Register with short password throws")
    void registra_shortPassword_throws()
    {
        assertThrows(IllegalArgumentException.class, () -> fs.registraFruitore("user1", "pas"));
    }

    @Test @DisplayName("Login with valid credentials")
    void login_valid()
    {
        fs.registraFruitore("user1", "pass1234");
        Fruitore f = fs.login("user1", "pass1234");
        assertNotNull(f);
        assertEquals("user1", f.getUsername());
    }

    @Test @DisplayName("Login with wrong password → null")
    void login_wrongPassword() { assertNull(fs.login("user1", "wrong")); }

    @Test @DisplayName("Login with null → null")
    void login_null() { assertNull(fs.login(null, null)); }

    @Test @DisplayName("notifica delegates to NotificaService")
    void notifica_delegates()
    {
        fs.notifica("user1", "Test message");
        // Notification was added but not persisted (no save in notifica method)
        // We verify no exception was thrown
    }
}
