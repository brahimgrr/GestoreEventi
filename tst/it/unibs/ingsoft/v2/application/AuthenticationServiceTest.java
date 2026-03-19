package it.unibs.ingsoft.v2.application;

import it.unibs.ingsoft.v2.domain.Configuratore;
import it.unibs.ingsoft.v2.persistence.api.IUtenteRepository;
import it.unibs.ingsoft.v2.persistence.dto.UtenteData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – AuthenticationService")
class AuthenticationServiceTest
{
    private AuthenticationService auth;

    @BeforeEach
    void setUp()
    {
        UtenteData utenti = new UtenteData();
        IUtenteRepository mockRepo = new IUtenteRepository()
        {
            @Override public UtenteData load()             { return utenti; }
            @Override public void save(UtenteData d)       {}
        };
        auth = new AuthenticationService(mockRepo, utenti);
    }

    @Test @DisplayName("Login with default credentials returns configuratore")
    void login_default()
    {
        assertTrue(auth.login(AuthenticationService.USERNAME_PREDEFINITO,
                              AuthenticationService.PASSWORD_PREDEFINITA).isPresent());
    }

    @Test @DisplayName("Login with saved credentials returns configuratore")
    void login_saved()
    {
        auth.registraNuovoConfiguratore("mario", "pass1234");
        assertTrue(auth.login("mario", "pass1234").isPresent());
    }

    @Test @DisplayName("Login with wrong password returns empty")
    void login_wrong()
    {
        assertTrue(auth.login("unknown", "wrong").isEmpty());
    }

    @Test @DisplayName("Register and login returns correct username")
    void register_and_login()
    {
        Configuratore c = auth.registraNuovoConfiguratore("mario", "pass1234");
        assertEquals("mario", c.getUsername());
    }

    @Test @DisplayName("Register duplicate username throws")
    void register_duplicate_throws()
    {
        auth.registraNuovoConfiguratore("mario", "pass1234");
        assertThrows(IllegalArgumentException.class,
                () -> auth.registraNuovoConfiguratore("mario", "pass5678"));
    }

    @Test @DisplayName("existeUsername returns true after registration")
    void esisteUsername_afterRegistration()
    {
        auth.registraNuovoConfiguratore("mario", "pass1234");
        assertTrue(auth.esisteUsername("mario"));
        assertFalse(auth.esisteUsername("unknown"));
    }
}
