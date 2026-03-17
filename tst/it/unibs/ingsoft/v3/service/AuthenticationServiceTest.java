package it.unibs.ingsoft.v3.service;

import it.unibs.ingsoft.v3.persistence.AppData;
import it.unibs.ingsoft.v3.persistence.IPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – AuthenticationService")
class AuthenticationServiceTest
{
    private AuthenticationService auth;

    @BeforeEach
    void setUp()
    {
        AppData d = new AppData();
        IPersistenceService db = new IPersistenceService()
        { @Override public AppData loadOrCreate() { return d; } @Override public void save(AppData x) {} };
        auth = new AuthenticationService(db, d);
    }

    @Test @DisplayName("Default login") void def() { assertNotNull(auth.login(AuthenticationService.USERNAME_PREDEFINITO, AuthenticationService.PASSWORD_PREDEFINITA)); }
    @Test @DisplayName("Register and login") void reg() { auth.registraNuovoConfiguratore("test", "pass"); assertNotNull(auth.login("test", "pass")); }
    @Test @DisplayName("Wrong creds") void wrong() { assertNull(auth.login("x", "y")); }
}
