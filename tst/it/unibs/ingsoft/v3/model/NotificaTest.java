package it.unibs.ingsoft.v3.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – Notifica")
class NotificaTest
{
    @Test @DisplayName("Constructor stores message and date")
    void constructor()
    {
        Notifica n = new Notifica("Hello", LocalDate.now());
        assertEquals("Hello", n.getMessaggio());
        assertEquals(LocalDate.now(), n.getData());
    }

    @Test @DisplayName("toString contains message")
    void toStringContainsMessage()
    {
        Notifica n = new Notifica("Test msg", LocalDate.of(2026, 1, 1));
        assertTrue(n.toString().contains("Test msg"));
    }
}
