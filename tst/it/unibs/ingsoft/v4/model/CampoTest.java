package it.unibs.ingsoft.v4.model;

import org.junit.jupiter.api.Test; import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("V4 – Campo") class CampoTest { @Test @DisplayName("Valid") void v() { assertEquals("T", new Campo("T", TipoCampo.BASE, TipoDato.STRINGA, true).getNome()); } @Test @DisplayName("Null throws") void n() { assertThrows(IllegalArgumentException.class, () -> new Campo(null, TipoCampo.BASE, TipoDato.STRINGA, true)); } }
