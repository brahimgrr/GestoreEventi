package it.unibs.ingsoft.v3;

import it.unibs.ingsoft.v3.application.IscrizioneService;
import it.unibs.ingsoft.v3.application.NotificationService;
import it.unibs.ingsoft.v3.application.PropostaService;
import it.unibs.ingsoft.v3.application.StateTransitionService;
import it.unibs.ingsoft.v3.domain.AppConstants;
import it.unibs.ingsoft.v3.domain.Bacheca;
import it.unibs.ingsoft.v3.domain.Categoria;
import it.unibs.ingsoft.v3.domain.Fruitore;
import it.unibs.ingsoft.v3.domain.Proposta;
import it.unibs.ingsoft.v3.domain.SpazioPersonale;
import it.unibs.ingsoft.v3.domain.StatoProposta;
import it.unibs.ingsoft.v3.persistence.api.IBachecaRepository;
import it.unibs.ingsoft.v3.persistence.api.ISpazioPersonaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class V3TimeBoundaryIntegrationTest {

    private PropostaService propostaService;
    private NotificationService notificationService;
    private StateTransitionService stateTransitionService;
    private IscrizioneService iscrizioneService;
    private Map<String, SpazioPersonale> spazi;

    @BeforeEach
    void setUp() {
        Bacheca bacheca = new Bacheca();
        spazi = new HashMap<>();

        IBachecaRepository bachecaRepo = new IBachecaRepository() {
            @Override
            public Bacheca get() {
                return bacheca;
            }

            @Override
            public void save() {
            }
        };

        ISpazioPersonaleRepository spazioRepo = new ISpazioPersonaleRepository() {
            @Override
            public SpazioPersonale get(String username) {
                return spazi.computeIfAbsent(username, ignored -> new SpazioPersonale());
            }

            @Override
            public void save() {
            }
        };

        propostaService = new PropostaService(bachecaRepo);
        notificationService = new NotificationService(spazioRepo);
        stateTransitionService = new StateTransitionService(bachecaRepo, notificationService);
        iscrizioneService = new IscrizioneService(bachecaRepo, stateTransitionService);

        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-10T10:00:00Z"), ZoneId.of("UTC"));
    }

    @Test
    void testIscrizione_AccettataAlle2359_RifiutataAlle0000() {
        Proposta proposta = buildAndPublishProposta(2, "11/01/2025", "14/01/2025", "14/01/2025");

        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-11T23:59:00Z"), ZoneId.of("UTC"));
        assertDoesNotThrow(() -> iscrizioneService.iscrivi(proposta, new Fruitore("alice")));
        assertEquals(List.of("alice"), proposta.getListaAderenti());

        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-12T00:00:00Z"), ZoneId.of("UTC"));
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> iscrizioneService.iscrivi(proposta, new Fruitore("bob")));

        assertTrue(ex.getMessage().contains("scaduto"));
        assertEquals(List.of("alice"), proposta.getListaAderenti());
        assertEquals(StatoProposta.APERTA, proposta.getStato());
    }

    @Test
    void testScadenza_ScattaSoloDopoMezzanotte_EControlloRipetutoEIdempotente() {
        Proposta proposta = buildAndPublishProposta(2, "11/01/2025", "14/01/2025", "14/01/2025");
        iscrizioneService.iscrivi(proposta, new Fruitore("alice"));

        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-11T23:59:00Z"), ZoneId.of("UTC"));
        stateTransitionService.controllaScadenze();
        assertEquals(StatoProposta.APERTA, proposta.getStato());
        assertTrue(notificationService.getNotifiche("alice").isEmpty());
        int historyBeforeMidnight = proposta.getStateHistory().size();

        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-12T00:00:00Z"), ZoneId.of("UTC"));
        stateTransitionService.controllaScadenze();
        assertEquals(StatoProposta.ANNULLATA, proposta.getStato());
        assertEquals(1, notificationService.getNotifiche("alice").size());
        int historyAfterTransition = proposta.getStateHistory().size();

        stateTransitionService.controllaScadenze();
        assertEquals(StatoProposta.ANNULLATA, proposta.getStato());
        assertEquals(1, notificationService.getNotifiche("alice").size());
        assertTrue(historyAfterTransition > historyBeforeMidnight);
        assertEquals(historyAfterTransition, proposta.getStateHistory().size());
    }

    private Proposta buildAndPublishProposta(int capacity, String termine, String dataEvento, String dataConclusiva) {
        Proposta proposta = propostaService.creaProposta(new Categoria("Sport"), new ArrayList<>(), new ArrayList<>());
        proposta.putAllValoriCampi(Map.of(
                PropostaService.CAMPO_TITOLO, "Partita",
                PropostaService.CAMPO_NUM_PARTECIPANTI, String.valueOf(capacity),
                PropostaService.CAMPO_TERMINE_ISCRIZIONE, termine,
                PropostaService.CAMPO_DATA, dataEvento,
                PropostaService.CAMPO_DATA_CONCLUSIVA, dataConclusiva,
                PropostaService.CAMPO_ORA, "18:00",
                PropostaService.CAMPO_LUOGO, "Campo"
        ));

        assertTrue(propostaService.validaProposta(proposta).isEmpty());
        propostaService.pubblicaProposta(proposta);
        return proposta;
    }
}
