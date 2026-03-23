package it.unibs.ingsoft.v1.integration;

import it.unibs.ingsoft.v1.application.AuthenticationService;
import it.unibs.ingsoft.v1.application.CatalogoService;
import it.unibs.ingsoft.v1.domain.Campo;
import it.unibs.ingsoft.v1.domain.CampoBaseDefinito;
import it.unibs.ingsoft.v1.domain.Configuratore;
import it.unibs.ingsoft.v1.domain.TipoDato;
import it.unibs.ingsoft.v1.persistence.impl.FileCatalogoRepository;
import it.unibs.ingsoft.v1.persistence.impl.FileCredenzialiRepository;
import it.unibs.ingsoft.v1.presentation.controller.AuthController;
import it.unibs.ingsoft.v1.presentation.controller.ConfiguratoreController;
import it.unibs.ingsoft.v1.support.ScriptedAppView;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class V1ConfigurationWorkflowIntegrationTest {

    @Test
    void shouldCompleteFirstAccessThenConfigureCommonAndSpecificFieldsEndToEnd() {
        Path testDir = createTestDir();
        Path usersFile = testDir.resolve("utenti.json");
        Path catalogoFile = testDir.resolve("catalogo.json");

        AuthenticationService authService = new AuthenticationService(new FileCredenzialiRepository(usersFile));
        AuthController authController = new AuthController(
                new ScriptedAppView()
                        .addStrings("config", "config", "mario")
                        .addPasswords("pass1")
                        .addYesNo(true),
                authService
        );

        Configuratore configuratore = authController.loginConfiguratore();
        CatalogoService catalogoService = new CatalogoService(new FileCatalogoRepository(catalogoFile));

        ScriptedAppView configView = new ScriptedAppView()
                .addYesNo(false, false, true, false, true)
                .addIntegers(
                        1, 1, 0,
                        2, 1, 3, 1, 1, 0, 0,
                        3, 0
                )
                .addStrings("Sponsor", "Sport", "Attrezzatura")
                .addTipoDati(TipoDato.STRINGA, TipoDato.STRINGA);

        ConfiguratoreController configuratoreController =
                new ConfiguratoreController(configuratore, configView, catalogoService);
        configuratoreController.run();

        AuthenticationService restartedAuth = new AuthenticationService(new FileCredenzialiRepository(usersFile));
        CatalogoService restartedCatalogo = new CatalogoService(new FileCatalogoRepository(catalogoFile));

        assertTrue(restartedAuth.login("mario", "pass1").isPresent());
        assertEquals(CampoBaseDefinito.values().length, restartedCatalogo.getCampiBase().size());
        assertEquals(List.of("Sponsor"), restartedCatalogo.getCampiComuni().stream().map(Campo::getNome).toList());
        assertEquals(1, restartedCatalogo.getCategorie().size());
        assertEquals("Sport", restartedCatalogo.getCategorie().get(0).getNome());
        assertEquals("Attrezzatura",
                restartedCatalogo.getCategorie().get(0).getCampiSpecifici().get(0).getNome());
        assertTrue(configView.containsOutput("Campo aggiunto."));
        assertTrue(configView.containsOutput("Categoria creata."));
    }

    private Path createTestDir() {
        try {
            Path dir = Path.of(".codex_build", "v1-workflow-tests", UUID.randomUUID().toString());
            Files.createDirectories(dir);
            return dir;
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile creare la cartella di test.", e);
        }
    }
}
