package it.unibs.ingsoft.v3.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – Configuratore")
class ConfiguratoreTest
{
    @Test @DisplayName("Is Persona") void isPers() { assertInstanceOf(Persona.class, new Configuratore("a")); }
    @Test @DisplayName("Stores username") void user() { assertEquals("a", new Configuratore("a").getUsername()); }
}
