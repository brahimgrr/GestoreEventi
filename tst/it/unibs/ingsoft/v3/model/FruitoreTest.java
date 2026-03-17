package it.unibs.ingsoft.v3.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – Fruitore")
class FruitoreTest
{
    @Test @DisplayName("Is a Persona subclass")
    void isPersona() { assertInstanceOf(Persona.class, new Fruitore("user1")); }

    @Test @DisplayName("Stores username")
    void storesUsername() { assertEquals("user1", new Fruitore("user1").getUsername()); }

    @Test @DisplayName("Null username throws")
    void nullUsername() { assertThrows(IllegalArgumentException.class, () -> new Fruitore(null)); }

    @Test @DisplayName("Blank username throws")
    void blankUsername() { assertThrows(IllegalArgumentException.class, () -> new Fruitore("  ")); }

    @Test @DisplayName("equals same username")
    void equals_same() { assertEquals(new Fruitore("u"), new Fruitore("u")); }

    @Test @DisplayName("equals different username")
    void equals_different() { assertNotEquals(new Fruitore("a"), new Fruitore("b")); }
}
