package it.unibs.ingsoft.v3.presentation.controller;

import it.unibs.ingsoft.v3.application.AuthenticationService;
import it.unibs.ingsoft.v3.domain.Configuratore;
import it.unibs.ingsoft.v3.domain.Credenziali;
import it.unibs.ingsoft.v3.domain.Fruitore;
import it.unibs.ingsoft.v3.persistence.api.ICredenzialiRepository;
import it.unibs.ingsoft.v3.support.ScriptedAppView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthControllerTest {

    private AuthenticationService authenticationService;
    private Credenziali credenziali;

    @BeforeEach
    void setUp() {
        credenziali = new Credenziali();
        ICredenzialiRepository repo = new ICredenzialiRepository() {
            @Override
            public Credenziali get() {
                return credenziali;
            }

            @Override
            public void save() {
            }
        };
        authenticationService = new AuthenticationService(repo);
    }

    @Test
    void testRegistraFruitore_PrimoAccessoRaccoglieCredenzialiIndividuali() {
        ScriptedAppView view = new ScriptedAppView()
                .addStrings("alice")
                .addPasswords("pass1")
                .addYesNo(true);

        AuthController controller = new AuthController(view, authenticationService);
        Fruitore registered = controller.registraFruitore();

        assertNotNull(registered);
        assertEquals("alice", registered.getUsername());
        assertTrue(authenticationService.loginFruitore("alice", "pass1").isPresent());
        assertTrue(view.containsOutput("REGISTRAZIONE FRUITORE"));
    }

    @Test
    void testLoginConfiguratore_CredenzialiPredefinite_ForzaRegistrazioneCredenzialiPersonali() {
        ScriptedAppView view = new ScriptedAppView()
                .addStrings("config", "config", "nuovoAdmin")
                .addPasswords("adminPass")
                .addYesNo(true);

        AuthController controller = new AuthController(view, authenticationService);
        Configuratore configuratore = controller.loginConfiguratore();

        assertNotNull(configuratore);
        assertEquals("nuovoAdmin", configuratore.getUsername());
        assertTrue(authenticationService.login("nuovoAdmin", "adminPass").isPresent());
        assertTrue(view.containsOutput("Primo accesso con credenziali predefinite."));
    }
}
