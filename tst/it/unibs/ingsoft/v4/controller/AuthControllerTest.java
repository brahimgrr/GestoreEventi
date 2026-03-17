package it.unibs.ingsoft.v4.controller;
import it.unibs.ingsoft.v4.service.AuthenticationService; import it.unibs.ingsoft.v4.persistence.AppData; import it.unibs.ingsoft.v4.persistence.IPersistenceService;
import org.junit.jupiter.api.Test; import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("V4 – AuthController") class AuthControllerTest { @Test @DisplayName("Default login") void d() { AppData data = new AppData(); IPersistenceService db = new IPersistenceService() { @Override public AppData loadOrCreate() { return data; } @Override public void save(AppData x) {} }; assertNotNull(new AuthenticationService(db, data).login(AuthenticationService.USERNAME_PREDEFINITO, AuthenticationService.PASSWORD_PREDEFINITA)); } }
