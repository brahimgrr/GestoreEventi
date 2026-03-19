package it.unibs.ingsoft.v1.application;

import it.unibs.ingsoft.v1.domain.Configuratore;
import it.unibs.ingsoft.v1.persistence.AppData;
import it.unibs.ingsoft.v1.persistence.IPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V1 – AuthenticationService")
class AuthenticationServiceTest
{
    private AppData data;
    private AuthenticationService auth;

    @BeforeEach
    void setUp()
    {
        data = new AppData();
        IPersistenceService mockDb = new IPersistenceService()
        {
            @Override public AppData loadOrCreate() { return data; }
            @Override public void save(AppData d) {}
        };
        auth = new AuthenticationService(mockDb, data);
    }

    // ───────────────────── Login ─────────────────────

    @Test
    @DisplayName("Login with default credentials returns Configuratore")
    void login_defaultCredentials_returnsConfiguratore()
    {
        Configuratore c = auth.login(
                AuthenticationService.USERNAME_PREDEFINITO,
                AuthenticationService.PASSWORD_PREDEFINITA);
        assertNotNull(c);
        assertEquals(AuthenticationService.USERNAME_PREDEFINITO, c.getUsername());
    }

    @Test
    @DisplayName("Login with saved credentials returns Configuratore")
    void login_savedCredentials_returnsConfiguratore()
    {
        auth.registraNuovoConfiguratore("mario", "pass1234");
        Configuratore c = auth.login("mario", "pass1234");
        assertNotNull(c);
        assertEquals("mario", c.getUsername());
    }

    @Test
    @DisplayName("Login with wrong password returns null")
    void login_wrongPassword_returnsNull()
    {
        auth.registraNuovoConfiguratore("mario", "pass1234");
        assertNull(auth.login("mario", "wrongpass"));
    }

    @Test
    @DisplayName("Login with unknown username returns null")
    void login_unknownUsername_returnsNull()
    {
        assertNull(auth.login("sconosciuto", "pass1234"));
    }

    @Test
    @DisplayName("Login with null username returns null")
    void login_nullUsername_returnsNull()
    {
        assertNull(auth.login(null, "pass1234"));
    }

    @Test
    @DisplayName("Login with null password returns null")
    void login_nullPassword_returnsNull()
    {
        assertNull(auth.login("mario", null));
    }

    // ───────────────────── Registration ─────────────────────

    @Test
    @DisplayName("Register new configurator with valid credentials")
    void registra_validCredentials_success()
    {
        Configuratore c = auth.registraNuovoConfiguratore("mario", "pass1234");
        assertNotNull(c);
        assertEquals("mario", c.getUsername());
    }

    @Test
    @DisplayName("Register with too short username throws")
    void registra_shortUsername_throwsException()
    {
        assertThrows(IllegalArgumentException.class,
                () -> auth.registraNuovoConfiguratore("ab", "pass1234"));
    }

    @Test
    @DisplayName("Register with too short password throws")
    void registra_shortPassword_throwsException()
    {
        assertThrows(IllegalArgumentException.class,
                () -> auth.registraNuovoConfiguratore("mario", "pas"));
    }

    @Test
    @DisplayName("Register with reserved username throws")
    void registra_reservedUsername_throwsException()
    {
        assertThrows(IllegalArgumentException.class,
                () -> auth.registraNuovoConfiguratore(
                        AuthenticationService.USERNAME_PREDEFINITO, "pass1234"));
    }

    @Test
    @DisplayName("Register with duplicate username throws")
    void registra_duplicateUsername_throwsException()
    {
        auth.registraNuovoConfiguratore("mario", "pass1234");
        assertThrows(IllegalArgumentException.class,
                () -> auth.registraNuovoConfiguratore("mario", "pass5678"));
    }

    @Test
    @DisplayName("Register with null username throws")
    void registra_nullUsername_throwsException()
    {
        assertThrows(IllegalArgumentException.class,
                () -> auth.registraNuovoConfiguratore(null, "pass1234"));
    }

    @Test
    @DisplayName("Register with blank password throws")
    void registra_blankPassword_throwsException()
    {
        assertThrows(IllegalArgumentException.class,
                () -> auth.registraNuovoConfiguratore("mario", "   "));
    }

    // ───────────────────── esistonoConfiguratori ─────────────────────

    @Test
    @DisplayName("esistonoConfiguratori returns false when empty")
    void esistonoConfiguratori_empty_returnsFalse()
    {
        assertFalse(auth.esistonoConfiguratori());
    }

    @Test
    @DisplayName("esistonoConfiguratori returns true after registration")
    void esistonoConfiguratori_afterRegistration_returnsTrue()
    {
        auth.registraNuovoConfiguratore("mario", "pass1234");
        assertTrue(auth.esistonoConfiguratori());
    }
}
