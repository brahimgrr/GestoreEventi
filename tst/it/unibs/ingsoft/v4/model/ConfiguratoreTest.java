package it.unibs.ingsoft.v4.model;

import org.junit.jupiter.api.Test; import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("V4 – Configuratore") class ConfiguratoreTest { @Test @DisplayName("Is Persona") void p() { assertInstanceOf(Persona.class, new Configuratore("a")); } @Test @DisplayName("Username") void u() { assertEquals("a", new Configuratore("a").getUsername()); } }
