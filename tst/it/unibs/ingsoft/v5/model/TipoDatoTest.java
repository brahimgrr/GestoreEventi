package it.unibs.ingsoft.v5.model;
import org.junit.jupiter.api.Test; import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("V5 – TipoDato") class TipoDatoTest { @Test @DisplayName("All 5 values") void v() { assertEquals(5, TipoDato.values().length); } }
