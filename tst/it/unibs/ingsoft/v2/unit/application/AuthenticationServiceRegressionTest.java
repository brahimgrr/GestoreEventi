package it.unibs.ingsoft.v2.unit.application;

import it.unibs.ingsoft.v2.application.AuthenticationService;
import it.unibs.ingsoft.v2.support.InMemoryCredenzialiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationServiceRegressionTest {

    private InMemoryCredenzialiRepository repo;
    private AuthenticationService authService;

    @BeforeEach
    void setUp() {
        repo = new InMemoryCredenzialiRepository();
        authService = new AuthenticationService(repo);
    }

    @Test
    void shouldAllowFirstLoginWithPredefinedCredentials() {
        assertTrue(authService.login("config", "config").isPresent());
    }

    @Test
    void shouldKeepPredefinedCredentialsAvailableAfterPersonalRegistration() {
        authService.registraNuovoConfiguratore("mario", "pass1");

        assertTrue(authService.login("config", "config").isPresent());
    }

    @Test
    void shouldRegisterConfiguratorPersistItAndAllowSubsequentLogin() {
        authService.registraNuovoConfiguratore("mario", "pass1");

        assertEquals(1, repo.getSaveCount());
        assertTrue(authService.login("mario", "pass1").isPresent());
        assertEquals("pass1", repo.get().getConfiguratori().get("mario"));
    }

    @Test
    void shouldRejectDuplicateUsername() {
        authService.registraNuovoConfiguratore("mario", "pass1");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.registraNuovoConfiguratore("mario", "pass2")
        );

        assertTrue(exception.getMessage().contains("Esiste"));
    }
}
