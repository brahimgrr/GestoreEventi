package it.unibs.ingsoft.v5.model;
import org.junit.jupiter.api.Test; import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("V5 – TipoCampo") class TipoCampoTest { @Test @DisplayName("All values") void v() { assertEquals(3, TipoCampo.values().length); } }
