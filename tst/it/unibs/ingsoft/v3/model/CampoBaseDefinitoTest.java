package it.unibs.ingsoft.v3.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – CampoBaseDefinito")
class CampoBaseDefinitoTest
{
    @Test @DisplayName("All 8 base fields") void all() { assertEquals(8, CampoBaseDefinito.values().length); }
    @Test @DisplayName("fromNome case-insensitive") void fromNome() { assertEquals(CampoBaseDefinito.TITOLO, CampoBaseDefinito.fromNome("titolo")); }
    @Test @DisplayName("isNomeFisso true") void isNomeFisso() { assertTrue(CampoBaseDefinito.isNomeFisso("Titolo")); }
}
