package it.unibs.ingsoft.v3.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – StatoProposta")
class StatoPropostaTest
{
    @Test @DisplayName("BOZZA → VALIDA ✓")
    void bozza_valida() { assertTrue(StatoProposta.BOZZA.canTransitionTo(StatoProposta.VALIDA)); }

    @Test @DisplayName("BOZZA → APERTA ✗")
    void bozza_aperta() { assertFalse(StatoProposta.BOZZA.canTransitionTo(StatoProposta.APERTA)); }

    @Test @DisplayName("VALIDA → APERTA ✓, BOZZA ✓")
    void valida_transitions()
    {
        assertTrue(StatoProposta.VALIDA.canTransitionTo(StatoProposta.APERTA));
        assertTrue(StatoProposta.VALIDA.canTransitionTo(StatoProposta.BOZZA));
    }

    @Test @DisplayName("APERTA → CONFERMATA ✓, ANNULLATA ✓")
    void aperta_transitions()
    {
        assertTrue(StatoProposta.APERTA.canTransitionTo(StatoProposta.CONFERMATA));
        assertTrue(StatoProposta.APERTA.canTransitionTo(StatoProposta.ANNULLATA));
    }

    @Test @DisplayName("CONFERMATA → CONCLUSA ✓")
    void confermata_conclusa() { assertTrue(StatoProposta.CONFERMATA.canTransitionTo(StatoProposta.CONCLUSA)); }

    @Test @DisplayName("ANNULLATA is terminal → no transitions")
    void annullata_terminal()
    {
        for (StatoProposta s : StatoProposta.values())
            assertFalse(StatoProposta.ANNULLATA.canTransitionTo(s));
    }

    @Test @DisplayName("All enum values count = 6")
    void allValues() { assertEquals(6, StatoProposta.values().length); }
}
