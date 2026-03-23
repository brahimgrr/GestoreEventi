package it.unibs.ingsoft.v2.unit.presentation;

import it.unibs.ingsoft.v2.application.AuthenticationService;
import it.unibs.ingsoft.v2.domain.Configuratore;
import it.unibs.ingsoft.v2.presentation.controller.AuthController;
import it.unibs.ingsoft.v2.support.InMemoryCredenzialiRepository;
import it.unibs.ingsoft.v2.support.ScriptedAppView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthControllerRegressionTest {

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(new InMemoryCredenzialiRepository());
    }

    @Test
    void shouldForcePersonalCredentialSelectionAfterPredefinedLogin() {
        ScriptedAppView view = new ScriptedAppView()
                .addStrings("config", "config", "nuovoAdmin")
                .addPasswords("pass1")
                .addYesNo(true);

        AuthController controller = new AuthController(view, authenticationService);
        Configuratore configuratore = controller.loginConfiguratore();

        assertEquals("nuovoAdmin", configuratore.getUsername());
        assertTrue(authenticationService.login("nuovoAdmin", "pass1").isPresent());
        assertTrue(view.containsOutput("Primo accesso con credenziali predefinite."));
    }
}
