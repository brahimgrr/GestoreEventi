package it.unibs.ingsoft.v3.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – Persona/Configuratore")
class PersonaTest
{
    @Test @DisplayName("Valid username") void valid() { assertEquals("m", new Configuratore("m").getUsername()); }
    @Test @DisplayName("Null throws") void nullN() { assertThrows(IllegalArgumentException.class, () -> new Configuratore(null)); }
    @Test @DisplayName("equals") void eq() { assertEquals(new Configuratore("m"), new Configuratore("m")); }
}
