package it.unibs.ingsoft.v3.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – Campo")
class CampoTest
{
    @Test @DisplayName("Valid construction") void valid() { assertEquals("T", new Campo("T", TipoCampo.BASE, TipoDato.STRINGA, true).getNome()); }
    @Test @DisplayName("Null name throws") void nullName() { assertThrows(IllegalArgumentException.class, () -> new Campo(null, TipoCampo.BASE, TipoDato.STRINGA, true)); }
    @Test @DisplayName("equals case-insensitive") void equalsCi() { assertEquals(new Campo("T", TipoCampo.BASE, TipoDato.STRINGA, true), new Campo("t", TipoCampo.BASE, TipoDato.INTERO, false)); }
}
