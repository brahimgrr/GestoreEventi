package it.unibs.ingsoft.v2.service;

import it.unibs.ingsoft.v2.model.Configuratore;
import it.unibs.ingsoft.v2.persistence.AppData;
import it.unibs.ingsoft.v2.persistence.IPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – AuthenticationService")
class AuthenticationServiceTest
{
    private AuthenticationService auth;

    @BeforeEach
    void setUp()
    {
        AppData data = new AppData();
        IPersistenceService mockDb = new IPersistenceService()
        {
            @Override public AppData loadOrCreate() { return data; }
            @Override public void save(AppData d) {}
        };
        auth = new AuthenticationService(mockDb, data);
    }

    @Test @DisplayName("Login with default credentials")
    void login_default()
    {
        assertNotNull(auth.login(AuthenticationService.USERNAME_PREDEFINITO, AuthenticationService.PASSWORD_PREDEFINITA));
    }

    @Test @DisplayName("Login with saved credentials")
    void login_saved()
    {
        auth.registraNuovoConfiguratore("mario", "pass1234");
        assertNotNull(auth.login("mario", "pass1234"));
    }

    @Test @DisplayName("Login wrong password")
    void login_wrong() { assertNull(auth.login("unknown", "wrong")); }

    @Test @DisplayName("Register and login")
    void register_and_login()
    {
        Configuratore c = auth.registraNuovoConfiguratore("mario", "pass1234");
        assertEquals("mario", c.getUsername());
    }

    @Test @DisplayName("Register duplicate throws")
    void register_duplicate_throws()
    {
        auth.registraNuovoConfiguratore("mario", "pass1234");
        assertThrows(IllegalArgumentException.class, () -> auth.registraNuovoConfiguratore("mario", "pass5678"));
    }
}
