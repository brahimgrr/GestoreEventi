package it.unibs.ingsoft.v4.model;

import org.junit.jupiter.api.Test; import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("V4 – Persona") class PersonaTest { @Test @DisplayName("Valid") void v() { assertEquals("m", new Configuratore("m").getUsername()); } @Test @DisplayName("Null throws") void n() { assertThrows(IllegalArgumentException.class, () -> new Configuratore(null)); } }
