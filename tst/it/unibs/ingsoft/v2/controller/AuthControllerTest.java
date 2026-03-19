package it.unibs.ingsoft.v2.controller;

import it.unibs.ingsoft.v2.application.AuthenticationService;
import it.unibs.ingsoft.v2.domain.Configuratore;
import it.unibs.ingsoft.v2.persistence.api.IUtenteRepository;
import it.unibs.ingsoft.v2.persistence.dto.UtenteData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – AuthController (service integration)")
class AuthControllerTest
{
    private AuthenticationService auth;

    @BeforeEach
    void setUp()
    {
        UtenteData utenti = new UtenteData();
        IUtenteRepository mockRepo = new IUtenteRepository()
        {
            @Override public UtenteData load()          { return utenti; }
            @Override public void save(UtenteData d)    {}
        };
        auth = new AuthenticationService(mockRepo, utenti);
    }

    @Test @DisplayName("Default credentials login returns present Optional")
    void defaultLogin()
    {
        assertTrue(auth.login(AuthenticationService.USERNAME_PREDEFINITO,
                              AuthenticationService.PASSWORD_PREDEFINITA).isPresent());
    }

    @Test @DisplayName("Personal credentials after registration work")
    void personalLogin()
    {
        auth.registraNuovoConfiguratore("mario", "pass1234");
        assertTrue(auth.login("mario", "pass1234").isPresent());
    }

    @Test @DisplayName("Wrong credentials return empty Optional")
    void wrongCredentials()
    {
        assertTrue(auth.login("nobody", "wrong").isEmpty());
    }
}
