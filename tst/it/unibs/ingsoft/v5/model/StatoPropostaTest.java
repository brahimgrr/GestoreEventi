package it.unibs.ingsoft.v5.model;
import org.junit.jupiter.api.Test; import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("V5 – StatoProposta") class StatoPropostaTest { @Test @DisplayName("All 7 states") void all() { assertEquals(7, StatoProposta.values().length); } @Test @DisplayName("APERTA→RITIRATA") void rit() { assertTrue(StatoProposta.APERTA.canTransitionTo(StatoProposta.RITIRATA)); } }
