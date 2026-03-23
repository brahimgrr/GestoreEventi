package it.unibs.ingsoft.v2.unit.presentation;

import it.unibs.ingsoft.v2.application.AuthenticationService;
import it.unibs.ingsoft.v2.domain.Configuratore;
import it.unibs.ingsoft.v2.presentation.controller.AuthController;
import it.unibs.ingsoft.v2.presentation.view.contract.OperationCancelledException;
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

    @Test
    void shouldRepeatRegistrationWhenFinalConfirmationIsDeclined() {
        ScriptedAppView view = new ScriptedAppView()
                .addStrings("config", "config", "mario", "luigi")
                .addPasswords("pass1", "pass2")
                .addYesNo(false, true);

        AuthController controller = new AuthController(view, authenticationService);
        Configuratore configuratore = controller.loginConfiguratore();

        assertEquals("luigi", configuratore.getUsername());
        assertTrue(view.containsOutput("Registrazione non confermata. Inserisci nuovamente i dati."));
        assertFalse(view.containsOutput("Registrazione annullata. Effettua nuovamente il login."));
    }

    @Test
    void shouldPropagateCancellationWhenLoginIsCancelled() {
        ScriptedAppView view = new ScriptedAppView()
                .addStrings("annulla");

        AuthController controller = new AuthController(view, authenticationService);

        assertThrows(OperationCancelledException.class, controller::loginConfiguratore);
    }
}
