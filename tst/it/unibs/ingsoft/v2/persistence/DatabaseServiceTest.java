package it.unibs.ingsoft.v2.persistence;

import it.unibs.ingsoft.v2.persistence.dto.UtenteData;
import it.unibs.ingsoft.v2.persistence.impl.FileUtenteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – FileUtenteRepository (serialization)")
class DatabaseServiceTest
{
    @TempDir Path tempDir;

    @Test @DisplayName("load returns empty UtenteData when no file exists")
    void load_fresh()
    {
        UtenteData data = new FileUtenteRepository(tempDir.resolve("u.ser")).load();
        assertNotNull(data);
        assertTrue(data.getConfiguratori().isEmpty());
    }

    @Test @DisplayName("save then load round-trip preserves data")
    void saveAndLoad()
    {
        Path p = tempDir.resolve("u.ser");
        FileUtenteRepository repo = new FileUtenteRepository(p);

        UtenteData data = new UtenteData();
        data.addConfiguratore("mario", "pass1234");
        repo.save(data);

        UtenteData loaded = new FileUtenteRepository(p).load();
        assertEquals("pass1234", loaded.getConfiguratori().get("mario"));
    }
}
