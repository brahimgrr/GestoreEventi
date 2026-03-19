package it.unibs.ingsoft.v1.controller;

import it.unibs.ingsoft.v1.domain.Configuratore;
import it.unibs.ingsoft.v1.application.AuthenticationService;
import it.unibs.ingsoft.v1.persistence.api.IUtenteRepository;
import it.unibs.ingsoft.v1.persistence.dto.UtenteData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Optional;

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
        UtenteData data = new UtenteData();
        IUtenteRepository mockRepo = new IUtenteRepository()
        {
            @Override public UtenteData load()        { return data; }
            @Override public void save(UtenteData d)  {}
        };
        auth = new AuthenticationService(mockRepo, data);
    }

    @Test
    @DisplayName("Default credentials login yields a configuratore that must register")
    void defaultLogin_requiresRegistration()
    {
        Optional<Configuratore> result = auth.login(
                AuthenticationService.USERNAME_PREDEFINITO,
                AuthenticationService.PASSWORD_PREDEFINITA);
        assertTrue(result.isPresent());
        assertEquals(AuthenticationService.USERNAME_PREDEFINITO, result.get().getUsername());
    }

    @Test
    @DisplayName("After registration, personal credentials work for login")
    void afterRegistration_personalCredentialsWork()
    {
        auth.registraNuovoConfiguratore("mario", "pass1234");
        Optional<Configuratore> result = auth.login("mario", "pass1234");
        assertTrue(result.isPresent());
        assertEquals("mario", result.get().getUsername());
    }

    @Test
    @DisplayName("Invalid credentials return empty Optional")
    void invalidCredentials_returnEmpty()
    {
        assertTrue(auth.login("unknown", "wrong").isEmpty());
    }

    @Test
    @DisplayName("esisteUsername returns false before registration, true after")
    void esisteUsername_beforeAndAfter()
    {
        assertFalse(auth.esisteUsername("mario"));
        auth.registraNuovoConfiguratore("mario", "pass1234");
        assertTrue(auth.esisteUsername("mario"));
    }
}
