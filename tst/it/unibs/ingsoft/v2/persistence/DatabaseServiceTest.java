package it.unibs.ingsoft.v2.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – DatabaseService")
class DatabaseServiceTest
{
    @TempDir Path tempDir;

    @Test @DisplayName("loadOrCreate returns fresh data when no file")
    void loadOrCreate_fresh() { assertNotNull(new DatabaseService(tempDir.resolve("d.ser")).loadOrCreate()); }

    @Test @DisplayName("save then load returns consistent data")
    void saveAndLoad()
    {
        Path p = tempDir.resolve("d.ser");
        DatabaseService db = new DatabaseService(p);
        AppData data = new AppData();
        data.addConfiguratore("u", "p");
        db.save(data);
        assertEquals("p", new DatabaseService(p).loadOrCreate().getConfiguratori().get("u"));
    }
}
