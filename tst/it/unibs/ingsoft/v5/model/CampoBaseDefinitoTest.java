package it.unibs.ingsoft.v5.model;
import org.junit.jupiter.api.Test; import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("V5 – CampoBaseDefinito") class CampoBaseDefinitoTest { @Test @DisplayName("All 8") void a() { assertEquals(8, CampoBaseDefinito.values().length); } @Test @DisplayName("fromNome") void f() { assertEquals(CampoBaseDefinito.TITOLO, CampoBaseDefinito.fromNome("titolo")); } }
