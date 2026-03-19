package it.unibs.ingsoft.v2.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – TipoCampo")
class TipoCampoTest
{
    @Test @DisplayName("All three enum values exist")
    void values() { assertEquals(3, TipoCampo.values().length); }

    @Test @DisplayName("valueOf round-trip")
    void valueOf_roundTrip() { assertEquals(TipoCampo.BASE, TipoCampo.valueOf("BASE")); }
}
