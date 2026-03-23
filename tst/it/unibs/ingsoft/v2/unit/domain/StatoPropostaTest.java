package it.unibs.ingsoft.v2.unit.domain;

import it.unibs.ingsoft.v2.domain.StatoProposta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatoPropostaTest {

    @Test
    void shouldAllowOnlyBozzaToValidaTransition() {
        assertTrue(StatoProposta.BOZZA.canTransitionTo(StatoProposta.VALIDA));
        assertFalse(StatoProposta.BOZZA.canTransitionTo(StatoProposta.APERTA));
    }

    @Test
    void shouldAllowValidaToBozzaAndAperta() {
        assertTrue(StatoProposta.VALIDA.canTransitionTo(StatoProposta.BOZZA));
        assertTrue(StatoProposta.VALIDA.canTransitionTo(StatoProposta.APERTA));
    }

    @Test
    void shouldDisallowAnyFurtherTransitionFromAperta() {
        for (StatoProposta state : StatoProposta.values()) {
            assertFalse(StatoProposta.APERTA.canTransitionTo(state));
        }
    }
}
