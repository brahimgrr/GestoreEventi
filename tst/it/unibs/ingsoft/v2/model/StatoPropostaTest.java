package it.unibs.ingsoft.v2.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – StatoProposta")
class StatoPropostaTest
{
    @Test
    @DisplayName("BOZZA can transition to VALIDA only")
    void bozza_canTransitionToValida()
    {
        assertTrue(StatoProposta.BOZZA.canTransitionTo(StatoProposta.VALIDA));
        assertFalse(StatoProposta.BOZZA.canTransitionTo(StatoProposta.APERTA));
        assertFalse(StatoProposta.BOZZA.canTransitionTo(StatoProposta.BOZZA));
    }

    @Test
    @DisplayName("VALIDA can transition to APERTA or BOZZA")
    void valida_canTransitionToApertaOrBozza()
    {
        assertTrue(StatoProposta.VALIDA.canTransitionTo(StatoProposta.APERTA));
        assertTrue(StatoProposta.VALIDA.canTransitionTo(StatoProposta.BOZZA));
        assertFalse(StatoProposta.VALIDA.canTransitionTo(StatoProposta.VALIDA));
    }

    @Test
    @DisplayName("APERTA cannot transition to anything (V2)")
    void aperta_cannotTransition()
    {
        assertFalse(StatoProposta.APERTA.canTransitionTo(StatoProposta.BOZZA));
        assertFalse(StatoProposta.APERTA.canTransitionTo(StatoProposta.VALIDA));
        assertFalse(StatoProposta.APERTA.canTransitionTo(StatoProposta.APERTA));
    }

    @Test
    @DisplayName("All enum values exist")
    void allValues_exist()
    {
        assertEquals(3, StatoProposta.values().length);
        assertNotNull(StatoProposta.BOZZA);
        assertNotNull(StatoProposta.VALIDA);
        assertNotNull(StatoProposta.APERTA);
    }
}
