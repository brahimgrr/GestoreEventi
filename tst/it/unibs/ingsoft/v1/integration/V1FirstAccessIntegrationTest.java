package it.unibs.ingsoft.v1.integration;

import it.unibs.ingsoft.v1.application.AuthenticationService;
import it.unibs.ingsoft.v1.persistence.impl.FileCredenzialiRepository;
import it.unibs.ingsoft.v1.presentation.controller.AuthController;
import it.unibs.ingsoft.v1.support.ScriptedAppView;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class V1FirstAccessIntegrationTest {

    @Test
    void shouldPersistFirstAccessRegistrationAcrossRestartAndKeepPredefinedCredentialsAvailable() {
        Path testDir = createTestDir();
        Path usersFile = testDir.resolve("utenti.json");

        AuthenticationService firstRunAuth = new AuthenticationService(new FileCredenzialiRepository(usersFile));
        ScriptedAppView firstRunView = new ScriptedAppView()
                .addStrings("config", "config", "mario")
                .addPasswords("pass1")
                .addYesNo(true);

        AuthController controller = new AuthController(firstRunView, firstRunAuth);
        assertEquals("mario", controller.loginConfiguratore().getUsername());

        AuthenticationService restartedAuth = new AuthenticationService(new FileCredenzialiRepository(usersFile));

        assertTrue(Files.exists(usersFile));
        assertTrue(restartedAuth.login("mario", "pass1").isPresent());
        assertTrue(restartedAuth.login("config", "config").isPresent());
    }

    private Path createTestDir() {
        try {
            Path dir = Path.of(".codex_build", "v1-first-access-tests", UUID.randomUUID().toString());
            Files.createDirectories(dir);
            return dir;
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile creare la cartella di test.", e);
        }
    }
}
