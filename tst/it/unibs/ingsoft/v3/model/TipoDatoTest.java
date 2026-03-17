package it.unibs.ingsoft.v3.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – TipoDato")
class TipoDatoTest
{
    @Test @DisplayName("All 5 values") void vals() { assertEquals(5, TipoDato.values().length); }
}
