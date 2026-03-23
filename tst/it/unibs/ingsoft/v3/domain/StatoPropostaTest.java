package it.unibs.ingsoft.v3.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatoPropostaTest {

    // =================================================================
    // BOZZA transitions
    // =================================================================

    @Test
    void testBozza_CanTransitionTo_Valida() {
        assertTrue(StatoProposta.BOZZA.canTransitionTo(StatoProposta.VALIDA));
    }

    @Test
    void testBozza_CannotTransitionTo_Aperta() {
        assertFalse(StatoProposta.BOZZA.canTransitionTo(StatoProposta.APERTA));
    }

    @Test
    void testBozza_CannotTransitionTo_Annullata() {
        assertFalse(StatoProposta.BOZZA.canTransitionTo(StatoProposta.ANNULLATA));
    }

    // =================================================================
    // VALIDA transitions
    // =================================================================

    @Test
    void testValida_CanTransitionTo_Aperta() {
        assertTrue(StatoProposta.VALIDA.canTransitionTo(StatoProposta.APERTA));
    }

    @Test
    void testValida_CanTransitionTo_Bozza() {
        // Revert path used by validaProposta idempotency
        assertTrue(StatoProposta.VALIDA.canTransitionTo(StatoProposta.BOZZA));
    }

    @Test
    void testValida_CannotTransitionTo_Confermata() {
        assertFalse(StatoProposta.VALIDA.canTransitionTo(StatoProposta.CONFERMATA));
    }

    // =================================================================
    // APERTA transitions
    // =================================================================

    @Test
    void testAperta_CanTransitionTo_Confermata() {
        assertTrue(StatoProposta.APERTA.canTransitionTo(StatoProposta.CONFERMATA));
    }

    @Test
    void testAperta_CanTransitionTo_Annullata() {
        assertTrue(StatoProposta.APERTA.canTransitionTo(StatoProposta.ANNULLATA));
    }

    @Test
    void testAperta_CannotTransitionTo_Conclusa() {
        // Must go through CONFERMATA first
        assertFalse(StatoProposta.APERTA.canTransitionTo(StatoProposta.CONCLUSA));
    }

    // =================================================================
    // CONFERMATA transitions
    // =================================================================

    @Test
    void testConfermata_CanTransitionTo_Conclusa() {
        assertTrue(StatoProposta.CONFERMATA.canTransitionTo(StatoProposta.CONCLUSA));
    }

    // =================================================================
    // Terminal states — no further transitions allowed
    // =================================================================

    @Test
    void testAnnullata_CannotTransitionToAnything() {
        for (StatoProposta s : StatoProposta.values()) {
            assertFalse(StatoProposta.ANNULLATA.canTransitionTo(s),
                    "ANNULLATA should not be able to transition to " + s);
        }
    }

    @Test
    void testConclusa_CannotTransitionToAnything() {
        for (StatoProposta s : StatoProposta.values()) {
            assertFalse(StatoProposta.CONCLUSA.canTransitionTo(s),
                    "CONCLUSA should not be able to transition to " + s);
        }
    }
}
