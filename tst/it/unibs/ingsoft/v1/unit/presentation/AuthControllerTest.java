package it.unibs.ingsoft.v1.unit.presentation;

import it.unibs.ingsoft.v1.application.AuthenticationService;
import it.unibs.ingsoft.v1.domain.Configuratore;
import it.unibs.ingsoft.v1.presentation.controller.AuthController;
import it.unibs.ingsoft.v1.support.InMemoryCredenzialiRepository;
import it.unibs.ingsoft.v1.support.ScriptedAppView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthControllerTest {

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(new InMemoryCredenzialiRepository());
    }

    @Test
    void shouldForceCredentialChangeOnFirstAccess() {
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
    void shouldDenyAccessUntilPersonalCredentialsAreChosen() {
        ScriptedAppView view = new ScriptedAppView()
                .addStrings("config", "config", "annulla", "x", "y", "config", "config", "mario")
                .addPasswords("pass1")
                .addYesNo(true);

        AuthController controller = new AuthController(view, authenticationService);
        Configuratore configuratore = controller.loginConfiguratore();

        assertEquals("mario", configuratore.getUsername());
        assertTrue(view.containsOutput("Registrazione annullata. Effettua nuovamente il login."));
        assertTrue(view.containsOutput("Credenziali non valide. Riprova."));
    }

    @Test
    void shouldRejectReservedUsernameDuringInteractiveRegistration() {
        ScriptedAppView view = new ScriptedAppView()
                .addStrings("config", "config", "config", "bob")
                .addPasswords("pass2")
                .addYesNo(true);

        AuthController controller = new AuthController(view, authenticationService);
        Configuratore configuratore = controller.loginConfiguratore();

        assertEquals("bob", configuratore.getUsername());
        assertTrue(view.containsOutput("Username riservato."));
    }

    @Test
    void shouldRepeatLoginAfterInvalidCredentials() {
        ScriptedAppView view = new ScriptedAppView()
                .addStrings("sbagliato", "credenziali", "config", "config", "marco")
                .addPasswords("pass1")
                .addYesNo(true);

        AuthController controller = new AuthController(view, authenticationService);
        Configuratore configuratore = controller.loginConfiguratore();

        assertEquals("marco", configuratore.getUsername());
        assertTrue(view.containsOutput("Credenziali non valide. Riprova."));
    }
}
