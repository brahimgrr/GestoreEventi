package it.unibs.ingsoft.v2.integration;

import it.unibs.ingsoft.v2.application.AuthenticationService;
import it.unibs.ingsoft.v2.application.CatalogoService;
import it.unibs.ingsoft.v2.domain.CampoBaseDefinito;
import it.unibs.ingsoft.v2.persistence.impl.FileCatalogoRepository;
import it.unibs.ingsoft.v2.persistence.impl.FileCredenzialiRepository;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class V2BackwardCompatibilityIntegrationTest {

    @Test
    void shouldPreserveV1AuthenticationAndConfigurationPersistence() {
        Path testDir = createTestDir();
        Path usersFile = testDir.resolve("utenti.json");
        Path catalogoFile = testDir.resolve("catalogo.json");

        AuthenticationService authService = new AuthenticationService(new FileCredenzialiRepository(usersFile));
        authService.registraNuovoConfiguratore("mario", "pass1");

        CatalogoService catalogoService = new CatalogoService(new FileCatalogoRepository(catalogoFile));
        catalogoService.initiateCampiBase();
        catalogoService.addCampoComune("Descrizione", it.unibs.ingsoft.v2.domain.TipoDato.STRINGA, true);
        catalogoService.createCategoria("Sport");
        catalogoService.addCampoSpecifico("Sport", "Attrezzatura", it.unibs.ingsoft.v2.domain.TipoDato.STRINGA, true);

        AuthenticationService restartedAuth = new AuthenticationService(new FileCredenzialiRepository(usersFile));
        CatalogoService restartedCatalogo = new CatalogoService(new FileCatalogoRepository(catalogoFile));

        assertTrue(restartedAuth.login("mario", "pass1").isPresent());
        assertTrue(restartedAuth.login("config", "config").isPresent());
        assertEquals(CampoBaseDefinito.values().length, restartedCatalogo.getCampiBase().size());
        assertEquals(1, restartedCatalogo.getCampiComuni().size());
        assertEquals(1, restartedCatalogo.getCategorie().size());
        assertEquals("Attrezzatura", restartedCatalogo.getCategorie().get(0).getCampiSpecifici().get(0).getNome());
    }

    private Path createTestDir() {
        try {
            Path dir = Path.of(".codex_build", "v2-regression-tests", UUID.randomUUID().toString());
            Files.createDirectories(dir);
            return dir;
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile creare la cartella di test.", e);
        }
    }
}
