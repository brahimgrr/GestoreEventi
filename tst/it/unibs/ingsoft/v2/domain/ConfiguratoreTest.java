package it.unibs.ingsoft.v2.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – Configuratore")
class ConfiguratoreTest
{
    @Test @DisplayName("Is a Persona subclass")
    void isPersona() { assertInstanceOf(Persona.class, new Configuratore("admin")); }

    @Test @DisplayName("Stores username")
    void storesUsername() { assertEquals("admin", new Configuratore("admin").getUsername()); }

    @Test @DisplayName("Equals with same username")
    void equals_same() { assertEquals(new Configuratore("admin"), new Configuratore("admin")); }
}
