package it.unibs.ingsoft.v3.application;

import it.unibs.ingsoft.v3.domain.Credenziali;
import it.unibs.ingsoft.v3.persistence.api.ICredenzialiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationServiceAdditionalTest {

    private AuthenticationService authService;
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
        authService = new AuthenticationService(repo);
    }

    @Test
    void testRegistraFruitore_UsernameGiaEsistenteComeFruitore_Fallisce() {
        authService.registraNuovoFruitore("alice", "pass1");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.registraNuovoFruitore("alice", "pass2"));

        assertTrue(ex.getMessage().contains("Esiste gi"));
        assertEquals(1, credenziali.getFruitori().size());
        assertEquals("pass1", credenziali.getFruitori().get("alice"));
    }

    @Test
    void testRegistraFruitore_CredenzialiRiservateOInvalide_FallisconoSenzaPersistenza() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> authService.registraNuovoFruitore(AuthenticationService.USERNAME_PREDEFINITO, "pass1")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> authService.registraNuovoFruitore("ab", "pass1")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> authService.registraNuovoFruitore("alice", "123")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> authService.registraNuovoFruitore("   ", "pass1")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> authService.registraNuovoFruitore("alice", "   "))
        );

        assertTrue(credenziali.getFruitori().isEmpty());
        assertTrue(credenziali.getConfiguratori().isEmpty());
    }
}
