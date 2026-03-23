package it.unibs.ingsoft.v1.integration;

import it.unibs.ingsoft.v1.application.CatalogoService;
import it.unibs.ingsoft.v1.domain.Campo;
import it.unibs.ingsoft.v1.domain.CampoBaseDefinito;
import it.unibs.ingsoft.v1.domain.Categoria;
import it.unibs.ingsoft.v1.domain.TipoDato;
import it.unibs.ingsoft.v1.persistence.impl.FileCatalogoRepository;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class V1PersistenceIntegrationTest {

    @Test
    void shouldPersistBaseCommonAndSpecificFieldsAcrossRestart() {
        Path testDir = createTestDir();
        Path catalogoFile = testDir.resolve("catalogo.json");

        CatalogoService firstRun = new CatalogoService(new FileCatalogoRepository(catalogoFile));
        firstRun.initiateCampiBase();
        firstRun.addCampoComune("Sponsor", TipoDato.STRINGA, false);
        firstRun.createCategoria("Sport");
        firstRun.addCampoSpecifico("Sport", "Attrezzatura", TipoDato.STRINGA, true);

        CatalogoService restarted = new CatalogoService(new FileCatalogoRepository(catalogoFile));
        List<Campo> commonFields = restarted.getCampiComuni();
        List<Categoria> categories = restarted.getCategorie();

        assertEquals(CampoBaseDefinito.values().length, restarted.getCampiBase().size());
        assertEquals(1, commonFields.size());
        assertEquals("Sponsor", commonFields.get(0).getNome());
        assertEquals(1, categories.size());
        assertEquals("Sport", categories.get(0).getNome());
        assertEquals("Attrezzatura", categories.get(0).getCampiSpecifici().get(0).getNome());
    }

    @Test
    void shouldKeepBaseFieldsImmutableAndPersistLaterUpdatesAcrossRestart() {
        Path testDir = createTestDir();
        Path catalogoFile = testDir.resolve("catalogo.json");

        CatalogoService firstRun = new CatalogoService(new FileCatalogoRepository(catalogoFile));
        firstRun.initiateCampiBase();
        firstRun.addCampoComune("Sponsor", TipoDato.STRINGA, false);
        firstRun.createCategoria("Sport");
        firstRun.addCampoSpecifico("Sport", "Attrezzatura", TipoDato.STRINGA, false);

        CatalogoService secondRun = new CatalogoService(new FileCatalogoRepository(catalogoFile));
        IllegalStateException exception = assertThrows(IllegalStateException.class, secondRun::initiateCampiBase);
        assertTrue(exception.getMessage().contains("Campi base"));

        assertTrue(secondRun.setObbligatorietaCampoComune("Sponsor", true));
        assertTrue(secondRun.removeCategoria("Sport"));

        CatalogoService thirdRun = new CatalogoService(new FileCatalogoRepository(catalogoFile));

        assertEquals(CampoBaseDefinito.values().length, thirdRun.getCampiBase().size());
        assertTrue(thirdRun.getCampiComuni().get(0).isObbligatorio());
        assertTrue(thirdRun.getCategorie().isEmpty());
    }

    private Path createTestDir() {
        try {
            Path dir = Path.of(".codex_build", "v1-persistence-tests", UUID.randomUUID().toString());
            Files.createDirectories(dir);
            return dir;
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile creare la cartella di test.", e);
        }
    }
}
