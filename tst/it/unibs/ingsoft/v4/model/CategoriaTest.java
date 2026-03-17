package it.unibs.ingsoft.v4.model;

import org.junit.jupiter.api.Test; import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("V4 – Categoria") class CategoriaTest { @Test @DisplayName("Valid") void v() { assertEquals("S", new Categoria("S").getNome()); } @Test @DisplayName("Null throws") void n() { assertThrows(IllegalArgumentException.class, () -> new Categoria(null)); } @Test @DisplayName("equals ci") void e() { assertEquals(new Categoria("S"), new Categoria("s")); } }
