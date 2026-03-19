package it.unibs.ingsoft.v2.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – TipoDato")
class TipoDatoTest
{
    @Test
    @DisplayName("All five enum values exist")
    void allValues_exist()
    {
        assertEquals(5, TipoDato.values().length);
        assertNotNull(TipoDato.STRINGA);
        assertNotNull(TipoDato.INTERO);
        assertNotNull(TipoDato.DECIMALE);
        assertNotNull(TipoDato.DATA);
        assertNotNull(TipoDato.BOOLEANO);
    }

    @Test
    @DisplayName("valueOf round-trip works")
    void valueOf_roundTrip()
    {
        for (TipoDato td : TipoDato.values())
            assertEquals(td, TipoDato.valueOf(td.name()));
    }
}
