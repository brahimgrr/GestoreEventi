package it.unibs.ingsoft.v3.domain;

import it.unibs.ingsoft.v3.domain.AppConstants;
import it.unibs.ingsoft.v3.application.PropostaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PropostaDomainTest {

    private Proposta proposta;

    @BeforeEach
    void setUp() {
        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-10T10:00:00Z"), ZoneId.of("UTC"));
        proposta = new Proposta(new Categoria("Sport"), new ArrayList<>(), new ArrayList<>());
    }

    // =================================================================
    // Initial state
    // =================================================================

    @Test
    void testNuovaProposta_StatoInizialeBozza() {
        assertEquals(StatoProposta.BOZZA, proposta.getStato());
    }

    @Test
    void testNuovaProposta_StateHistoryHasBozza() {
        // Constructor (line 54) adds the BOZZA entry immediately
        List<PropostaStateChange> history = proposta.getStateHistory();
        assertEquals(1, history.size());
        assertEquals(StatoProposta.BOZZA, history.get(0).getStato());
        assertNotNull(history.get(0).getDataCambio());
    }

    // =================================================================
    // setStato — valid transitions
    // =================================================================

    @Test
    void testSetStato_TransizioneValida_AggiornaStateHistory() {
        proposta.setStato(StatoProposta.VALIDA);

        List<PropostaStateChange> history = proposta.getStateHistory();
        assertEquals(2, history.size());
        assertEquals(StatoProposta.BOZZA, history.get(0).getStato());
        assertEquals(StatoProposta.VALIDA, history.get(1).getStato());
        assertNotNull(history.get(1).getDataCambio());
    }

    // =================================================================
    // setStato — invalid transitions
    // =================================================================

    @Test
    void testSetStato_TransizioneNonValida_LanciaEccezione() {
        // BOZZA → CONFERMATA is not a valid transition
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> proposta.setStato(StatoProposta.CONFERMATA));
        assertTrue(ex.getMessage().contains("Transizione non valida"));
    }

    @Test
    void testSetStato_NullStato_LanciaEccezione() {
        assertThrows(IllegalArgumentException.class,
                () -> proposta.setStato(null));
    }

    // =================================================================
    // addAderente
    // =================================================================

    @Test
    void testAddAderente_AggiunteCorrette() {
        proposta.setStato(StatoProposta.VALIDA);
        proposta.setStato(StatoProposta.APERTA);
        proposta.addAderente("mario");
        List<String> aderenti = proposta.getListaAderenti();
        assertEquals(1, aderenti.size());
        assertTrue(aderenti.contains("mario"));
    }

    @Test
    void testAddAderente_DuplicatoIgnorato() {
        proposta.setStato(StatoProposta.VALIDA);
        proposta.setStato(StatoProposta.APERTA);
        proposta.addAderente("mario");
        proposta.addAderente("mario"); // duplicate call
        assertEquals(1, proposta.getListaAderenti().size());
    }

    // =================================================================
    // getNumeroPartecipanti
    // =================================================================

    @Test
    void testGetNumeroPartecipanti_ParsesCorrectly() {
        proposta.putAllValoriCampi(Map.of(PropostaService.CAMPO_NUM_PARTECIPANTI, "5"));
        assertEquals(5, proposta.getNumeroPartecipanti());

        // Non-numeric value falls back to 0
        proposta.putAllValoriCampi(Map.of(PropostaService.CAMPO_NUM_PARTECIPANTI, "abc"));
        assertEquals(0, proposta.getNumeroPartecipanti());
    }
}
