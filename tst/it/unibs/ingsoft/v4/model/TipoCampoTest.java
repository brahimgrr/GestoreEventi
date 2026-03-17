package it.unibs.ingsoft.v4.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;
@DisplayName("V4 – TipoCampo")
class TipoCampoTest {
    @Test @DisplayName("All values")
    void v() {
        assertEquals(3, TipoCampo.values().length);
    }
}
