package it.unibs.ingsoft.v3.controller;

import it.unibs.ingsoft.v3.service.AuthenticationService;
import it.unibs.ingsoft.v3.persistence.AppData;
import it.unibs.ingsoft.v3.persistence.IPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – AuthController")
class AuthControllerTest
{
    private AuthenticationService auth;
    @BeforeEach void setUp()
    {
        AppData d = new AppData();
        IPersistenceService db = new IPersistenceService()
        { @Override public AppData loadOrCreate() { return d; } @Override public void save(AppData x) {} };
        auth = new AuthenticationService(db, d);
    }
    @Test @DisplayName("Default login") void def() { assertNotNull(auth.login(AuthenticationService.USERNAME_PREDEFINITO, AuthenticationService.PASSWORD_PREDEFINITA)); }
    @Test @DisplayName("Register + login") void reg() { auth.registraNuovoConfiguratore("test", "pass1234"); assertNotNull(auth.login("test", "pass1234")); }
}
