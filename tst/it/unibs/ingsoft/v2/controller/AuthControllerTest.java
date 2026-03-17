package it.unibs.ingsoft.v2.controller;

import it.unibs.ingsoft.v2.service.AuthenticationService;
import it.unibs.ingsoft.v2.persistence.AppData;
import it.unibs.ingsoft.v2.persistence.IPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – AuthController")
class AuthControllerTest
{
    private AuthenticationService auth;

    @BeforeEach
    void setUp()
    {
        AppData data = new AppData();
        IPersistenceService mockDb = new IPersistenceService()
        {
            @Override public AppData loadOrCreate() { return data; }
            @Override public void save(AppData d) {}
        };
        auth = new AuthenticationService(mockDb, data);
    }

    @Test @DisplayName("Default credentials login works")
    void defaultLogin() { assertNotNull(auth.login(AuthenticationService.USERNAME_PREDEFINITO, AuthenticationService.PASSWORD_PREDEFINITA)); }

    @Test @DisplayName("Personal credentials after registration work")
    void personalLogin()
    {
        auth.registraNuovoConfiguratore("mario", "pass1234");
        assertNotNull(auth.login("mario", "pass1234"));
    }
}
