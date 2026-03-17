package it.unibs.ingsoft.v1.controller;

import it.unibs.ingsoft.v1.model.Configuratore;
import it.unibs.ingsoft.v1.service.AuthenticationService;
import it.unibs.ingsoft.v1.persistence.AppData;
import it.unibs.ingsoft.v1.persistence.IPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthController coordinates UI and AuthenticationService.
 * Here we test that the underlying service interactions work correctly.
 * Full controller tests with mocked views are integration-level and
 * require simulating Scanner input, so we validate the service layer
 * that the controller delegates to.
 */
@DisplayName("V1 – AuthController (service integration)")
class AuthControllerTest
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

    @Test
    @DisplayName("Default credentials login yields a configuratore that must register")
    void defaultLogin_requiresRegistration()
    {
        Configuratore c = auth.login(
                AuthenticationService.USERNAME_PREDEFINITO,
                AuthenticationService.PASSWORD_PREDEFINITA);
        assertNotNull(c);
        assertEquals(AuthenticationService.USERNAME_PREDEFINITO, c.getUsername());
    }

    @Test
    @DisplayName("After registration, personal credentials work for login")
    void afterRegistration_personalCredentialsWork()
    {
        auth.registraNuovoConfiguratore("mario", "pass1234");
        Configuratore c = auth.login("mario", "pass1234");
        assertNotNull(c);
        assertEquals("mario", c.getUsername());
    }

    @Test
    @DisplayName("Invalid credentials do not authenticate")
    void invalidCredentials_returnNull()
    {
        assertNull(auth.login("unknown", "wrong"));
    }
}
