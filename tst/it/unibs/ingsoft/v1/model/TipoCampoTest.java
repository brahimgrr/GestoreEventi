package it.unibs.ingsoft.v1.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V1 – TipoCampo")
class TipoCampoTest
{
    @Test
    @DisplayName("All three enum values exist")
    void values_allThreePresent()
    {
        TipoCampo[] values = TipoCampo.values();
        assertEquals(3, values.length);
        assertNotNull(TipoCampo.BASE);
        assertNotNull(TipoCampo.COMUNE);
        assertNotNull(TipoCampo.SPECIFICO);
    }

    @Test
    @DisplayName("valueOf round-trip works")
    void valueOf_roundTrip()
    {
        assertEquals(TipoCampo.BASE, TipoCampo.valueOf("BASE"));
        assertEquals(TipoCampo.COMUNE, TipoCampo.valueOf("COMUNE"));
        assertEquals(TipoCampo.SPECIFICO, TipoCampo.valueOf("SPECIFICO"));
    }

    @Test
    @DisplayName("valueOf with invalid name throws IllegalArgumentException")
    void valueOf_invalidName_throwsException()
    {
        assertThrows(IllegalArgumentException.class, () -> TipoCampo.valueOf("INVALIDO"));
    }
}
