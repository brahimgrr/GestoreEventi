package it.unibs.ingsoft.v2.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – Persona (via Configuratore)")
class PersonaTest
{
    @Test @DisplayName("Valid username is trimmed")
    void constructor_trims() { assertEquals("mario", new Configuratore("  mario  ").getUsername()); }

    @Test @DisplayName("Null username throws")
    void constructor_null() { assertThrows(IllegalArgumentException.class, () -> new Configuratore(null)); }

    @Test @DisplayName("Blank username throws")
    void constructor_blank() { assertThrows(IllegalArgumentException.class, () -> new Configuratore("  ")); }

    @Test @DisplayName("equals same username")
    void equals_same() { assertEquals(new Configuratore("a"), new Configuratore("a")); }

    @Test @DisplayName("equals different username")
    void equals_different() { assertNotEquals(new Configuratore("a"), new Configuratore("b")); }
}
