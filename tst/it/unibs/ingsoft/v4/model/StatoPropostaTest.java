package it.unibs.ingsoft.v4.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V4 – StatoProposta")
class StatoPropostaTest
{
    @Test @DisplayName("All 7 states exist") void all() { assertEquals(7, StatoProposta.values().length); }
    @Test @DisplayName("APERTA → RITIRATA ✓") void apertaRit() { assertTrue(StatoProposta.APERTA.canTransitionTo(StatoProposta.RITIRATA)); }
    @Test @DisplayName("CONFERMATA → RITIRATA ✓") void confRit() { assertTrue(StatoProposta.CONFERMATA.canTransitionTo(StatoProposta.RITIRATA)); }
    @Test @DisplayName("RITIRATA is terminal") void ritTerminal()
    { for (StatoProposta s : StatoProposta.values()) assertFalse(StatoProposta.RITIRATA.canTransitionTo(s)); }
    @Test @DisplayName("BOZZA → RITIRATA ✗") void bozzaRit() { assertFalse(StatoProposta.BOZZA.canTransitionTo(StatoProposta.RITIRATA)); }
    @Test @DisplayName("ANNULLATA still terminal") void annTerminal()
    { for (StatoProposta s : StatoProposta.values()) assertFalse(StatoProposta.ANNULLATA.canTransitionTo(s)); }
}
