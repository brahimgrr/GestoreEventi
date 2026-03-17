package it.unibs.ingsoft.v4.model;

import org.junit.jupiter.api.Test; import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("V4 – Fruitore") class FruitoreTest { @Test @DisplayName("Is Persona") void p() { assertInstanceOf(Persona.class, new Fruitore("u")); } @Test @DisplayName("Username") void u() { assertEquals("u", new Fruitore("u").getUsername()); } @Test @DisplayName("Null throws") void n() { assertThrows(IllegalArgumentException.class, () -> new Fruitore(null)); } }
