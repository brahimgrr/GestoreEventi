package it.unibs.ingsoft.v3.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – TipoCampo")
class TipoCampoTest
{
    @Test @DisplayName("All values") void vals() { assertEquals(3, TipoCampo.values().length); }
}
