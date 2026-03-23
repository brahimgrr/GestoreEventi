package it.unibs.ingsoft.v1.unit.application;

import it.unibs.ingsoft.v1.application.AuthenticationService;
import it.unibs.ingsoft.v1.domain.Configuratore;
import it.unibs.ingsoft.v1.support.InMemoryCredenzialiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationServiceTest {

    private InMemoryCredenzialiRepository repo;
    private AuthenticationService authService;

    @BeforeEach
    void setUp() {
        repo = new InMemoryCredenzialiRepository();
        authService = new AuthenticationService(repo);
    }

    @Test
    void shouldAllowFirstLoginWithPredefinedCredentials() {
        Optional<Configuratore> result = authService.login("config", "config");

        assertTrue(result.isPresent());
        assertEquals("config", result.get().getUsername());
    }

    @Test
    void shouldKeepPredefinedCredentialsAvailableAfterPersonalRegistration() {
        authService.registraNuovoConfiguratore("mario", "pass1");

        Optional<Configuratore> result = authService.login("config", "config");

        assertTrue(result.isPresent());
        assertEquals("config", result.get().getUsername());
    }

    @Test
    void shouldRegisterConfiguratorPersistCredentialsAndAllowSubsequentLogin() {
        Configuratore configuratore = authService.registraNuovoConfiguratore("mario", "pass1");

        assertEquals("mario", configuratore.getUsername());
        assertEquals(1, repo.getSaveCount());
        assertEquals("pass1", repo.get().getConfiguratori().get("mario"));
        assertTrue(authService.login("mario", "pass1").isPresent());
    }

    @Test
    void shouldRejectReservedUsername() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.registraNuovoConfiguratore("config", "pass1")
        );

        assertTrue(exception.getMessage().contains("riservato"));
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

    @Test
    void shouldRejectTooShortUsernameOrPassword() {
        IllegalArgumentException usernameException = assertThrows(
                IllegalArgumentException.class,
                () -> authService.registraNuovoConfiguratore("ab", "pass1")
        );
        IllegalArgumentException passwordException = assertThrows(
                IllegalArgumentException.class,
                () -> authService.registraNuovoConfiguratore("mario", "abc")
        );

        assertTrue(usernameException.getMessage().contains("almeno 3"));
        assertTrue(passwordException.getMessage().contains("almeno 4"));
    }

    @Test
    void shouldReturnEmptyForInvalidOrNullLoginCredentials() {
        assertTrue(authService.login("mario", "pass1").isEmpty());
        assertTrue(authService.login(null, "pass1").isEmpty());
        assertTrue(authService.login("mario", null).isEmpty());
    }

    @Test
    void shouldReportExistingUsernamesAfterRegistration() {
        authService.registraNuovoConfiguratore("mario", "pass1");

        assertTrue(authService.esisteUsername("mario"));
        assertFalse(authService.esisteUsername("luigi"));
        assertFalse(authService.esisteUsername(null));
    }
}
