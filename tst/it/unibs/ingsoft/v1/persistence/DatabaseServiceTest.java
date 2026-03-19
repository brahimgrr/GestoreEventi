package it.unibs.ingsoft.v1.persistence;

import it.unibs.ingsoft.v1.persistence.api.IUtenteRepository;
import it.unibs.ingsoft.v1.persistence.dto.UtenteData;
import it.unibs.ingsoft.v1.persistence.impl.FileUtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Previously tested the deleted DatabaseService (monolithic serialization).
 * Now tests FileUtenteRepository — the replacement for the user-data portion.
 */
@DisplayName("V1 – FileUtenteRepository")
class DatabaseServiceTest
{
    @TempDir
    Path tempDir;

    private IUtenteRepository repo;

    @BeforeEach
    void setUp()
    {
        repo = new FileUtenteRepository(tempDir.resolve("utenti.json"));
    }

    @Test
    @DisplayName("load returns fresh UtenteData when no file exists")
    void load_noFile_returnsFreshData()
    {
        UtenteData data = repo.load();
        assertNotNull(data);
        assertTrue(data.getConfiguratori().isEmpty());
    }

    @Test
    @DisplayName("save then load returns consistent user data")
    void saveAndLoad_dataConsistent()
    {
        UtenteData data = new UtenteData();
        data.addConfiguratore("mario", "pass1234");
        repo.save(data);

        IUtenteRepository repo2 = new FileUtenteRepository(tempDir.resolve("utenti.json"));
        UtenteData loaded = repo2.load();

        assertEquals("pass1234", loaded.getConfiguratori().get("mario"));
    }

    @Test
    @DisplayName("save creates parent directories if needed")
    void save_createsDirectories()
    {
        Path nested = tempDir.resolve("sub").resolve("dir").resolve("utenti.json");
        IUtenteRepository nestedRepo = new FileUtenteRepository(nested);

        assertDoesNotThrow(() -> nestedRepo.save(new UtenteData()));
    }

    @Test
    @DisplayName("Multiple save/load cycles maintain consistency")
    void multipleSaveLoad_consistent()
    {
        UtenteData data = repo.load();
        data.addConfiguratore("user1", "pass1");
        repo.save(data);

        UtenteData loaded1 = repo.load();
        loaded1.addConfiguratore("user2", "pass2");
        repo.save(loaded1);

        UtenteData loaded2 = repo.load();
        assertEquals(2, loaded2.getConfiguratori().size());
    }
}
