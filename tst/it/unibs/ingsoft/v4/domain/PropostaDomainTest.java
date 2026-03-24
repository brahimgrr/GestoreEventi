package it.unibs.ingsoft.v4.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropostaDomainTest {

    private Clock originalClock;

    @BeforeEach
    void setUp() {
        originalClock = AppConstants.clock;
        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-09T10:00:00Z"), ZoneId.of("Europe/Rome"));
    }

    @AfterEach
    void tearDown() {
        AppConstants.clock = originalClock;
    }

    @Test
    void test_setStato_toRitirataFromAperta_recordsStateChangeDate() {
        Proposta proposta = createOpenProposal();

        proposta.setStato(StatoProposta.RITIRATA);

        assertEquals(StatoProposta.RITIRATA, proposta.getStato());
        assertEquals(
                List.of(StatoProposta.BOZZA, StatoProposta.VALIDA, StatoProposta.APERTA, StatoProposta.RITIRATA),
                proposta.getStateHistory().stream().map(PropostaStateChange::getStato).toList()
        );
        PropostaStateChange last = proposta.getStateHistory().get(proposta.getStateHistory().size() - 1);
        assertEquals(LocalDate.of(2025, 1, 9), last.getDataCambio());
    }

    @Test
    void test_addAderente_whenProposalIsWithdrawn_throwsIllegalStateException() {
        Proposta proposta = createOpenProposal();
        proposta.addAderente("alice");
        proposta.setStato(StatoProposta.RITIRATA);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> proposta.addAderente("bob"));

        assertTrue(ex.getMessage().contains("non"));
        assertEquals(List.of("alice"), proposta.getListaAderenti());
    }

    @Test
    void test_removeAderente_whenProposalIsWithdrawn_throwsIllegalStateException() {
        Proposta proposta = createOpenProposal();
        proposta.addAderente("alice");
        proposta.setStato(StatoProposta.RITIRATA);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> proposta.removeAderente("alice"));

        assertTrue(ex.getMessage().contains("non"));
        assertEquals(List.of("alice"), proposta.getListaAderenti());
    }

    @Test
    void test_statoProposta_ritirataTransitionAllowedOnlyFromApertaAndConfermata() {
        assertTrue(StatoProposta.APERTA.canTransitionTo(StatoProposta.RITIRATA));
        assertTrue(StatoProposta.CONFERMATA.canTransitionTo(StatoProposta.RITIRATA));
        assertFalse(StatoProposta.BOZZA.canTransitionTo(StatoProposta.RITIRATA));
        assertFalse(StatoProposta.VALIDA.canTransitionTo(StatoProposta.RITIRATA));
        assertFalse(StatoProposta.RITIRATA.canTransitionTo(StatoProposta.APERTA));
    }

    private Proposta createOpenProposal() {
        Proposta proposta = new Proposta(new Categoria("Sport"), new ArrayList<>(), new ArrayList<>());
        proposta.putAllValoriCampi(Map.of(
                AppConstants.CAMPO_TITOLO, "Partita",
                AppConstants.CAMPO_TERMINE_ISCRIZIONE, "10/01/2025",
                AppConstants.CAMPO_DATA, "12/01/2025",
                AppConstants.CAMPO_DATA_CONCLUSIVA, "12/01/2025",
                AppConstants.CAMPO_ORA, "18:00",
                AppConstants.CAMPO_LUOGO, "Campo",
                AppConstants.CAMPO_NUM_PARTECIPANTI, "3"
        ));
        proposta.setTermineIscrizione(LocalDate.parse("10/01/2025", AppConstants.DATE_FMT));
        proposta.setDataEvento(LocalDate.parse("12/01/2025", AppConstants.DATE_FMT));
        proposta.setStato(StatoProposta.VALIDA);
        proposta.setStato(StatoProposta.APERTA);
        return proposta;
    }
}
