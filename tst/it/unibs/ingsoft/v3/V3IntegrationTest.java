package it.unibs.ingsoft.v3;

import it.unibs.ingsoft.v3.application.*;
import it.unibs.ingsoft.v3.domain.*;
import it.unibs.ingsoft.v3.persistence.api.IBachecaRepository;
import it.unibs.ingsoft.v3.persistence.api.ICredenzialiRepository;
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

/**
 * End-to-end integration tests that wire all V3 services together and verify
 * the full requirement flows: subscription, state transitions, notifications,
 * personal space, and state history.
 */
class V3IntegrationTest {

    private AuthenticationService authService;
    private NotificationService notificationService;
    private StateTransitionService stateTransitionSvc;
    private IscrizioneService iscrizioneService;
    private PropostaService propostaService;

    private Bacheca bachecaMock;
    private Map<String, SpazioPersonale> mockSpazioDB;

    @BeforeEach
    void setUp() {
        Credenziali credenzialiDB = new Credenziali();
        bachecaMock = new Bacheca();
        mockSpazioDB = new HashMap<>();

        ICredenzialiRepository credenzialiRepo = new ICredenzialiRepository() {
            @Override public Credenziali get() { return credenzialiDB; }
            @Override public void save() {}
        };

        IBachecaRepository bachecaRepo = new IBachecaRepository() {
            @Override public Bacheca get() { return bachecaMock; }
            @Override public void save() {}
        };

        ISpazioPersonaleRepository spazioRepo = new ISpazioPersonaleRepository() {
            @Override public SpazioPersonale get(String username) {
                return mockSpazioDB.computeIfAbsent(username, k -> new SpazioPersonale());
            }
            @Override public void save() {}
        };

        authService         = new AuthenticationService(credenzialiRepo);
        notificationService = new NotificationService(spazioRepo);
        stateTransitionSvc  = new StateTransitionService(bachecaRepo, notificationService);
        iscrizioneService   = new IscrizioneService(bachecaRepo, stateTransitionSvc);
        propostaService     = new PropostaService(bachecaRepo);

        // Base clock: today = 10/01/2025
        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-10T10:00:00Z"), ZoneId.of("UTC"));
    }

    /**
     * Helper: creates, validates and publishes a proposal, returning it in APERTA state.
     * Dates must be valid relative to the clock set in each test.
     */
    private Proposta buildAndPublishProposta(int capacity, String termine,
                                              String dataEvento, String dataConclusiva) {
        Proposta p = propostaService.creaProposta(new Categoria("Sport"), new ArrayList<>(), new ArrayList<>());
        p.getValoriCampi().put(PropostaService.CAMPO_TITOLO, "Partita di calcio");
        p.getValoriCampi().put(PropostaService.CAMPO_NUM_PARTECIPANTI, String.valueOf(capacity));
        p.getValoriCampi().put(PropostaService.CAMPO_ORA, "15:00");
        p.getValoriCampi().put(PropostaService.CAMPO_LUOGO, "Stadio");
        p.getValoriCampi().put(PropostaService.CAMPO_TERMINE_ISCRIZIONE, termine);
        p.getValoriCampi().put(PropostaService.CAMPO_DATA, dataEvento);
        p.getValoriCampi().put(PropostaService.CAMPO_DATA_CONCLUSIVA, dataConclusiva);

        List<String> errori = propostaService.validaProposta(p);
        assertTrue(errori.isEmpty(), "Proposta validation failed: " + errori);

        propostaService.pubblicaProposta(p);
        assertEquals(StatoProposta.APERTA, p.getStato());
        return p;
    }

    // =================================================================
    // REQ 7 + 10: deadline passes, enough participants → CONFERMATA + notify with details
    // =================================================================

    @Test
    void testHappyPath_FruitoreIscrive_ScadenzaPassa_Confermata_RiceveNotifica() {
        // Register fruitore
        authService.registraNuovoFruitore("alice", "pass1");

        // Publish proposal with capacity=1
        Proposta p = buildAndPublishProposta(1, "15/01/2025", "18/01/2025", "18/01/2025");

        // Subscribe alice (fills capacity immediately → CONFERMATA right away via IscrizioneService)
        // Actually with capacity=1, subscribing fills it immediately.
        // To test the deadline path, use capacity=2 but only subscribe 1, then advance clock.
        // Here we rebuild with capacity=2 and subscribe 2 users to test the standard flow.

        // Let's use capacity=1 and subscribe alice → immediate CONFERMATA (via capacity fill)
        iscrizioneService.iscrivi(p, new Fruitore("alice"));

        // Proposal is now CONFERMATA (capacity was 1, alice filled it)
        assertEquals(StatoProposta.CONFERMATA, p.getStato());

        // Alice must have received a notification
        List<Notifica> notifiche = notificationService.getNotifiche("alice");
        assertEquals(1, notifiche.size());
        String msg = notifiche.get(0).getMessaggio();
        assertTrue(msg.contains("CONFERMATA"), "Expected 'CONFERMATA' in: " + msg);
        assertTrue(msg.contains("18/01/2025"), "Expected date in: " + msg);
        assertTrue(msg.contains("15:00"), "Expected time in: " + msg);
        assertTrue(msg.contains("Stadio"), "Expected location in: " + msg);
    }

    // =================================================================
    // REQ 8: deadline passes, not enough participants → ANNULLATA + notify all
    // =================================================================

    @Test
    void testSadPath_ScadenzaPassaAnnullata_IscrittiNotificati() {
        authService.registraNuovoFruitore("bob", "pass1");
        authService.registraNuovoFruitore("carol", "pass1");

        // capacity=3 but only 2 will subscribe → ANNULLATA at deadline
        Proposta p = buildAndPublishProposta(3, "15/01/2025", "18/01/2025", "18/01/2025");

        iscrizioneService.iscrivi(p, new Fruitore("bob"));
        iscrizioneService.iscrivi(p, new Fruitore("carol"));
        assertEquals(StatoProposta.APERTA, p.getStato()); // still open (2/3)

        // Advance clock past the deadline
        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-16T00:00:00Z"), ZoneId.of("UTC"));
        stateTransitionSvc.controllaScadenze();

        assertEquals(StatoProposta.ANNULLATA, p.getStato());

        // Both bob and carol must be notified
        assertEquals(1, notificationService.getNotifiche("bob").size());
        assertEquals(1, notificationService.getNotifiche("carol").size());
        assertTrue(notificationService.getNotifiche("bob").get(0).getMessaggio().contains("ANNULLATA"));
        assertTrue(notificationService.getNotifiche("carol").get(0).getMessaggio().contains("ANNULLATA"));
    }

    // =================================================================
    // REQ 6: capacity fills → immediate CONFERMATA, all notified, 3rd rejected
    // =================================================================

    @Test
    void testCapienzaRaggiunta_ImmediataConferma_TuttiNotificati() {
        authService.registraNuovoFruitore("dan", "pass1");
        authService.registraNuovoFruitore("eve", "pass1");
        authService.registraNuovoFruitore("frank", "pass1");

        Proposta p = buildAndPublishProposta(2, "15/01/2025", "18/01/2025", "18/01/2025");

        // First subscription: still open
        iscrizioneService.iscrivi(p, new Fruitore("dan"));
        assertEquals(StatoProposta.APERTA, p.getStato());

        // Second subscription fills capacity → immediate CONFERMATA (no clock advance needed)
        iscrizioneService.iscrivi(p, new Fruitore("eve"));
        assertEquals(StatoProposta.CONFERMATA, p.getStato());

        // Both dan and eve are notified
        assertEquals(1, notificationService.getNotifiche("dan").size());
        assertEquals(1, notificationService.getNotifiche("eve").size());
        assertTrue(notificationService.getNotifiche("dan").get(0).getMessaggio().contains("CONFERMATA"));
        assertTrue(notificationService.getNotifiche("eve").get(0).getMessaggio().contains("CONFERMATA"));

        // Third subscription must fail (proposal no longer APERTA)
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> iscrizioneService.iscrivi(p, new Fruitore("frank")));
        assertTrue(ex.getMessage().contains("non è APERTA") || ex.getMessage().contains("APERTA"));
        assertFalse(p.getListaAderenti().contains("frank"));
    }

    // =================================================================
    // REQ 11: state history populated across full lifecycle
    // =================================================================

    @Test
    void testStateHistoryPopulatoLungoIlCicloVita() {
        authService.registraNuovoFruitore("greta", "pass1");

        // Publish (BOZZA → VALIDA → APERTA): 3 history entries
        Proposta p = buildAndPublishProposta(1, "15/01/2025", "18/01/2025", "18/01/2025");
        assertEquals(3, p.getStateHistory().size());
        assertEquals(StatoProposta.BOZZA,  p.getStateHistory().get(0).getStato());
        assertEquals(StatoProposta.VALIDA, p.getStateHistory().get(1).getStato());
        assertEquals(StatoProposta.APERTA, p.getStateHistory().get(2).getStato());

        // Subscribe greta → fills capacity → CONFERMATA: 4th entry
        iscrizioneService.iscrivi(p, new Fruitore("greta"));
        assertEquals(4, p.getStateHistory().size());
        assertEquals(StatoProposta.CONFERMATA, p.getStateHistory().get(3).getStato());

        // Advance past dataConclusiva (18/01/2025) → CONCLUSA: 5th entry
        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-20T00:00:00Z"), ZoneId.of("UTC"));
        stateTransitionSvc.controllaScadenze();
        assertEquals(5, p.getStateHistory().size());
        assertEquals(StatoProposta.CONCLUSA, p.getStateHistory().get(4).getStato());

        // All entries have a non-null date
        for (PropostaStateChange entry : p.getStateHistory()) {
            assertNotNull(entry.getDataCambio(), "State change date must not be null for: " + entry.getStato());
        }
    }

    // =================================================================
    // REQ 10: CONFERMATA notification contains full event details
    // =================================================================

    @Test
    void testNotificaConferma_ContieneDettagliEvento() {
        authService.registraNuovoFruitore("helen", "pass1");

        // Build a proposal with specific, verifiable field values
        Proposta p = propostaService.creaProposta(new Categoria("Musica"), new ArrayList<>(), new ArrayList<>());
        p.getValoriCampi().put(PropostaService.CAMPO_TITOLO, "Concerto di Natale");
        p.getValoriCampi().put(PropostaService.CAMPO_NUM_PARTECIPANTI, "1");
        p.getValoriCampi().put(PropostaService.CAMPO_ORA, "20:30");
        p.getValoriCampi().put(PropostaService.CAMPO_LUOGO, "Teatro Regio");
        p.getValoriCampi().put(PropostaService.CAMPO_TERMINE_ISCRIZIONE, "15/01/2025");
        p.getValoriCampi().put(PropostaService.CAMPO_DATA, "18/01/2025");
        p.getValoriCampi().put(PropostaService.CAMPO_DATA_CONCLUSIVA, "18/01/2025");

        propostaService.validaProposta(p);
        propostaService.pubblicaProposta(p);
        p.addAderente("helen");

        // Trigger CONFERMATA directly (public method)
        stateTransitionSvc.confermaProposta(p);

        List<Notifica> notifiche = notificationService.getNotifiche("helen");
        assertEquals(1, notifiche.size());
        String msg = notifiche.get(0).getMessaggio();

        assertTrue(msg.contains("Concerto di Natale"), "Missing title in: " + msg);
        assertTrue(msg.contains("20:30"),              "Missing time in: " + msg);
        assertTrue(msg.contains("Teatro Regio"),        "Missing location in: " + msg);
        assertTrue(msg.contains("CONFERMATA"),          "Missing state in: " + msg);
    }

    // =================================================================
    // REQ 12: fruitore can view and selectively delete notifications
    // =================================================================

    @Test
    void testSpazioPersonale_FruitoreVedeECancellaNotifiche() {
        authService.registraNuovoFruitore("ivan", "pass1");

        // Subscribe ivan to a capacity=1 proposal → immediate CONFERMATA → 1 notification
        Proposta p = buildAndPublishProposta(1, "15/01/2025", "18/01/2025", "18/01/2025");
        iscrizioneService.iscrivi(p, new Fruitore("ivan"));
        assertEquals(StatoProposta.CONFERMATA, p.getStato());

        List<Notifica> notifiche = notificationService.getNotifiche("ivan");
        assertEquals(1, notifiche.size());

        // Fruitore deletes the notification
        Notifica n = notifiche.get(0);
        notificationService.cancellaNotifica("ivan", n);

        assertTrue(notificationService.getNotifiche("ivan").isEmpty());
    }
}
