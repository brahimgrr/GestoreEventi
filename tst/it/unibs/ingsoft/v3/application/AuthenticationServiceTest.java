package it.unibs.ingsoft.v3.application;

import it.unibs.ingsoft.v3.domain.Configuratore;
import it.unibs.ingsoft.v3.domain.Credenziali;
import it.unibs.ingsoft.v3.domain.Fruitore;
import it.unibs.ingsoft.v3.persistence.api.ICredenzialiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationServiceTest {

    private AuthenticationService authService;
    private Credenziali credenzialiDB;

    @BeforeEach
    void setUp() {
        credenzialiDB = new Credenziali();
        ICredenzialiRepository repo = new ICredenzialiRepository() {
            @Override
            public Credenziali get() {
                return credenzialiDB;
            }

            @Override
            public void save() {
                // In memoria, `credenzialiDB` è già aggiornato per riferimento
            }
        };

        authService = new AuthenticationService(repo);
    }

    @Test
    void testLoginConfiguratore_Predefinito() {
        Optional<Configuratore> result = authService.login("config", "config");
        assertTrue(result.isPresent());
        assertEquals("config", result.get().getUsername());
    }

    @Test
    void testRegistraConfiguratore_NominativoGiaEsistente_Fallisce() {
        authService.registraNuovoConfiguratore("admin1", "pass");
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            authService.registraNuovoConfiguratore("admin1", "pass2")
        );
        assertTrue(ex.getMessage().contains("Esiste già un utente"));
    }

    @Test
    void testRegistraFruitore_NominativoGiaEsistenteComeConfiguratore_Fallisce() {
        authService.registraNuovoConfiguratore("admin1", "pass");
        
        // Non deve essere possibile creare un fruitore con lo stesso username
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            authService.registraNuovoFruitore("admin1", "pass2")
        );
        assertTrue(ex.getMessage().contains("Esiste già un utente"));
    }

    @Test
    void testRegistraFruitore_LoginCorretto() {
        Fruitore f = authService.registraNuovoFruitore("utente1", "pass");
        assertNotNull(f);
        
        Optional<Fruitore> loginSuccesso = authService.loginFruitore("utente1", "pass");
        assertTrue(loginSuccesso.isPresent());
        
        Optional<Fruitore> loginFallito = authService.loginFruitore("utente1", "errata");
        assertFalse(loginFallito.isPresent());
    }

    @Test
    void testUsernameCondiviso() {
        authService.registraNuovoFruitore("comune", "pass");
        assertTrue(authService.esisteUsername("comune"));
        
        authService.registraNuovoConfiguratore("adminX", "pass");
        assertTrue(authService.esisteUsername("adminX"));
        
        assertFalse(authService.esisteUsername("inesistente"));
    }
}
