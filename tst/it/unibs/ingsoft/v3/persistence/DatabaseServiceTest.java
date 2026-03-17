package it.unibs.ingsoft.v3.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – DatabaseService")
class DatabaseServiceTest
{
    @TempDir Path tempDir;
    @Test @DisplayName("Save/load round-trip") void roundTrip()
    {
        Path p = tempDir.resolve("d.ser");
        DatabaseService db = new DatabaseService(p);
        AppData data = new AppData();
        data.addFruitore("u", "p");
        db.save(data);
        assertEquals("p", new DatabaseService(p).loadOrCreate().getFruitori().get("u"));
    }
}
