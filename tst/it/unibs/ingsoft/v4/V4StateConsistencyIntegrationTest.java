package it.unibs.ingsoft.v4;

import it.unibs.ingsoft.v4.application.IscrizioneService;
import it.unibs.ingsoft.v4.application.NotificationService;
import it.unibs.ingsoft.v4.application.PropostaService;
import it.unibs.ingsoft.v4.application.StateTransitionService;
import it.unibs.ingsoft.v4.domain.AppConstants;
import it.unibs.ingsoft.v4.domain.Bacheca;
import it.unibs.ingsoft.v4.domain.Categoria;
import it.unibs.ingsoft.v4.domain.Fruitore;
import it.unibs.ingsoft.v4.domain.Notifica;
import it.unibs.ingsoft.v4.domain.Proposta;
import it.unibs.ingsoft.v4.domain.PropostaStateChange;
import it.unibs.ingsoft.v4.domain.SpazioPersonale;
import it.unibs.ingsoft.v4.domain.StatoProposta;
import it.unibs.ingsoft.v4.persistence.api.IBachecaRepository;
import it.unibs.ingsoft.v4.persistence.api.ISpazioPersonaleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class V4StateConsistencyIntegrationTest {

    private Clock originalClock;
    private PropostaService propostaService;
    private StateTransitionService stateTransitionService;
    private IscrizioneService iscrizioneService;
    private NotificationService notificationService;
    private Map<String, SpazioPersonale> spazi;

    @BeforeEach
    void setUp() {
        originalClock = AppConstants.clock;
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
        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-10T09:00:00Z"), ZoneId.of("Europe/Rome"));
    }

    @AfterEach
    void tearDown() {
        AppConstants.clock = originalClock;
    }

    @Test
    void test_cancelledUsers_areNotInFinalWithdrawnParticipantList_andReceiveNoWithdrawalNotification() {
        Proposta proposta = buildAndPublishProposal("Evento cancellazioni", 4, "15/01/2025", "20/01/2025");
        Fruitore alice = new Fruitore("alice");
        Fruitore bob = new Fruitore("bob");
        Fruitore charlie = new Fruitore("charlie");

        iscrizioneService.iscrivi(proposta, alice);
        iscrizioneService.iscrivi(proposta, bob);
        iscrizioneService.disiscrivi(proposta, bob);
        iscrizioneService.iscrivi(proposta, charlie);

        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-19T12:00:00Z"), ZoneId.of("Europe/Rome"));
        stateTransitionService.ritiraProposta(proposta);

        assertEquals(StatoProposta.RITIRATA, proposta.getStato());
        assertEquals(List.of("alice", "charlie"), proposta.getListaAderenti());
        assertEquals(1, notificationService.getNotifiche("alice").size());
        assertEquals(1, notificationService.getNotifiche("charlie").size());
        assertTrue(notificationService.getNotifiche("bob").isEmpty());
    }

    @Test
    void test_withdrawnProposal_remainsInvisibleAfterRepeatedDeadlineChecks() {
        Proposta proposta = buildAndPublishProposal("Evento ritirato", 3, "15/01/2025", "20/01/2025");
        iscrizioneService.iscrivi(proposta, new Fruitore("alice"));

        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-19T12:00:00Z"), ZoneId.of("Europe/Rome"));
        stateTransitionService.ritiraProposta(proposta);
        int notificationCount = notificationService.getNotifiche("alice").size();
        int historySize = proposta.getStateHistory().size();

        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-25T12:00:00Z"), ZoneId.of("Europe/Rome"));
        stateTransitionService.controllaScadenze();

        assertEquals(StatoProposta.RITIRATA, proposta.getStato());
        assertTrue(propostaService.getBacheca().isEmpty());
        assertEquals(notificationCount, notificationService.getNotifiche("alice").size());
        assertEquals(historySize, proposta.getStateHistory().size());
    }

    @Test
    void test_expiredOpenProposal_annulsAndThenCannotBeWithdrawn() {
        Proposta proposta = buildAndPublishProposal("Evento scaduto", 3, "11/01/2025", "14/01/2025");
        iscrizioneService.iscrivi(proposta, new Fruitore("alice"));

        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-11T23:00:00Z"), ZoneId.of("Europe/Rome"));
        stateTransitionService.controllaScadenze();

        assertEquals(StatoProposta.ANNULLATA, proposta.getStato());
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> stateTransitionService.ritiraProposta(proposta));
        assertTrue(ex.getMessage().contains("APERTA") || ex.getMessage().contains("CONFERMATA"));
    }

    @Test
    void test_confirmedThenWithdrawn_stateHistoryPreservesExactTransitionSequence() {
        Proposta proposta = buildAndPublishProposal("Evento confermato e ritirato", 2, "15/01/2025", "20/01/2025");
        iscrizioneService.iscrivi(proposta, new Fruitore("alice"));
        iscrizioneService.iscrivi(proposta, new Fruitore("bob"));

        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-19T12:00:00Z"), ZoneId.of("Europe/Rome"));
        stateTransitionService.ritiraProposta(proposta);

        assertEquals(
                List.of(StatoProposta.BOZZA, StatoProposta.VALIDA, StatoProposta.APERTA, StatoProposta.CONFERMATA, StatoProposta.RITIRATA),
                proposta.getStateHistory().stream().map(PropostaStateChange::getStato).toList()
        );
    }

    @Test
    void test_confirmedProposal_afterWithdrawalDeadlineCannotBeWithdrawn_andParticipantsStayUnchanged() {
        Proposta proposta = buildAndPublishProposal("Evento tardivo", 2, "15/01/2025", "18/01/2025");
        iscrizioneService.iscrivi(proposta, new Fruitore("alice"));
        iscrizioneService.iscrivi(proposta, new Fruitore("bob"));

        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-17T23:00:00Z"), ZoneId.of("Europe/Rome"));
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> stateTransitionService.ritiraProposta(proposta));

        assertTrue(ex.getMessage().contains("giorno precedente"));
        assertEquals(StatoProposta.CONFERMATA, proposta.getStato());
        assertEquals(List.of("alice", "bob"), proposta.getListaAderenti());
        assertEquals(1, notificationService.getNotifiche("alice").size());
        assertEquals(1, notificationService.getNotifiche("bob").size());
        assertFalse(messages("alice").get(0).contains("RITIRATA"));
    }

    private Proposta buildAndPublishProposal(String titolo, int maxParticipants, String termine, String dataEvento) {
        Proposta proposta = propostaService.creaProposta(new Categoria("Cultura"), new ArrayList<>(), new ArrayList<>());
        proposta.putAllValoriCampi(Map.of(
                PropostaService.CAMPO_TITOLO, titolo,
                PropostaService.CAMPO_NUM_PARTECIPANTI, String.valueOf(maxParticipants),
                PropostaService.CAMPO_TERMINE_ISCRIZIONE, termine,
                PropostaService.CAMPO_DATA, dataEvento,
                PropostaService.CAMPO_DATA_CONCLUSIVA, dataEvento,
                PropostaService.CAMPO_ORA, "10:00",
                PropostaService.CAMPO_LUOGO, titolo
        ));

        assertTrue(propostaService.validaProposta(proposta).isEmpty());
        propostaService.pubblicaProposta(proposta);
        return proposta;
    }

    private List<String> messages(String username) {
        return notificationService.getNotifiche(username).stream().map(Notifica::getMessaggio).toList();
    }
}
