package it.unibs.ingsoft.v1.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V1 – DatabaseService")
class DatabaseServiceTest
{
    @TempDir
    Path tempDir;

    private DatabaseService db;
    private Path storagePath;

    @BeforeEach
    void setUp()
    {
        storagePath = tempDir.resolve("test_data.ser");
        db = new DatabaseService(storagePath);
    }

    @Test
    @DisplayName("loadOrCreate returns fresh AppData when no file exists")
    void loadOrCreate_noFile_returnsFreshData()
    {
        AppData data = db.loadOrCreate();
        assertNotNull(data);
        assertTrue(data.getConfiguratori().isEmpty());
        assertTrue(data.getCampiBase().isEmpty());
    }

    @Test
    @DisplayName("save then loadOrCreate returns consistent data")
    void saveAndLoad_dataConsistent()
    {
        AppData data = new AppData();
        data.addConfiguratore("mario", "pass1234");
        data.setCampiBaseFissati(true);

        db.save(data);

        // Reload from a new instance
        DatabaseService db2 = new DatabaseService(storagePath);
        AppData loaded = db2.loadOrCreate();

        assertEquals("pass1234", loaded.getConfiguratori().get("mario"));
        assertTrue(loaded.isCampiBaseFissati());
    }

    @Test
    @DisplayName("save creates parent directories if needed")
    void save_createsDirectories()
    {
        Path nested = tempDir.resolve("sub").resolve("dir").resolve("data.ser");
        DatabaseService dbNested = new DatabaseService(nested);

        AppData data = new AppData();
        assertDoesNotThrow(() -> dbNested.save(data));
    }

    @Test
    @DisplayName("Multiple save/load cycles maintain consistency")
    void multipleSaveLoad_consistent()
    {
        AppData data = db.loadOrCreate();
        data.addConfiguratore("user1", "pass1");
        db.save(data);

        AppData loaded1 = db.loadOrCreate();
        loaded1.addConfiguratore("user2", "pass2");
        db.save(loaded1);

        AppData loaded2 = db.loadOrCreate();
        assertEquals(2, loaded2.getConfiguratori().size());
    }
}
