package it.unibs.ingsoft.v4;

import it.unibs.ingsoft.v4.application.IscrizioneService;
import it.unibs.ingsoft.v4.application.NotificationService;
import it.unibs.ingsoft.v4.application.PropostaService;
import it.unibs.ingsoft.v4.application.StateTransitionService;
import it.unibs.ingsoft.v4.domain.AppConstants;
import it.unibs.ingsoft.v4.domain.Bacheca;
import it.unibs.ingsoft.v4.domain.Categoria;
import it.unibs.ingsoft.v4.domain.Fruitore;
import it.unibs.ingsoft.v4.domain.Proposta;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class V4TimeBoundaryIntegrationTest {

    private Clock originalClock;
    private PropostaService propostaService;
    private StateTransitionService stateTransitionService;
    private IscrizioneService iscrizioneService;

    @BeforeEach
    void setUp() {
        originalClock = AppConstants.clock;
        Bacheca bacheca = new Bacheca();
        Map<String, SpazioPersonale> spazi = new HashMap<>();

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
        NotificationService notificationService = new NotificationService(spazioRepo);
        stateTransitionService = new StateTransitionService(bachecaRepo, notificationService);
        iscrizioneService = new IscrizioneService(bachecaRepo, stateTransitionService);
        setClock("2025-01-10T09:00:00Z", "Europe/Rome");
    }

    @AfterEach
    void tearDown() {
        AppConstants.clock = originalClock;
    }

    @Test
    void test_cancelRegistration_exactlyAt2359Allowed_rejoinAllowed_afterMidnightRejected() {
        Proposta proposta = buildAndPublishProposal(3, "11/01/2025", "14/01/2025");
        Fruitore alice = new Fruitore("alice");

        iscrizioneService.iscrivi(proposta, alice);

        setClock("2025-01-11T22:59:00Z", "Europe/Rome");
        assertDoesNotThrow(() -> iscrizioneService.disiscrivi(proposta, alice));
        assertDoesNotThrow(() -> iscrizioneService.iscrivi(proposta, alice));
        assertEquals(List.of("alice"), proposta.getListaAderenti());

        setClock("2025-01-11T23:00:00Z", "Europe/Rome");
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> iscrizioneService.disiscrivi(proposta, alice));

        assertTrue(ex.getMessage().contains("scaduto"));
        assertEquals(List.of("alice"), proposta.getListaAderenti());
        assertEquals(StatoProposta.APERTA, proposta.getStato());
    }

    @Test
    void test_withdrawal_exactlyAt2359DayBeforeAllowed_at0000EventDayRejected() {
        Proposta allowed = buildAndPublishProposal("Partita di calcetto A", 3, "11/01/2025", "13/01/2025");
        iscrizioneService.iscrivi(allowed, new Fruitore("alice"));
        Proposta rejected = buildAndPublishProposal("Partita di calcetto B", 3, "11/01/2025", "13/01/2025");
        iscrizioneService.iscrivi(rejected, new Fruitore("bob"));

        setClock("2025-01-12T22:59:00Z", "Europe/Rome");
        assertDoesNotThrow(() -> stateTransitionService.ritiraProposta(allowed));
        assertEquals(StatoProposta.RITIRATA, allowed.getStato());

        setClock("2025-01-12T23:00:00Z", "Europe/Rome");
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> stateTransitionService.ritiraProposta(rejected));

        assertTrue(ex.getMessage().contains("giorno precedente"));
        assertEquals(StatoProposta.APERTA, rejected.getStato());
        assertEquals(List.of("bob"), rejected.getListaAderenti());
    }

    @Test
    void test_timezoneAssumption_sameInstantCanChangeCancellationEligibility() {
        Proposta proposta = buildAndPublishProposal("Partita di calcetto", 3, "11/01/2025", "14/01/2025");
        Fruitore alice = new Fruitore("alice");
        iscrizioneService.iscrivi(proposta, alice);

        setClock("2025-01-11T23:30:00Z", "UTC");
        assertDoesNotThrow(() -> iscrizioneService.disiscrivi(proposta, alice));
        iscrizioneService.iscrivi(proposta, alice);

        setClock("2025-01-11T23:30:00Z", "Europe/Rome");
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> iscrizioneService.disiscrivi(proposta, alice));

        assertTrue(ex.getMessage().contains("scaduto"));
        assertEquals(List.of("alice"), proposta.getListaAderenti());
    }

    private Proposta buildAndPublishProposal(int maxParticipants, String termine, String dataEvento) {
        return buildAndPublishProposal("Partita di calcetto", maxParticipants, termine, dataEvento);
    }

    private Proposta buildAndPublishProposal(String titolo, int maxParticipants, String termine, String dataEvento) {
        Proposta proposta = propostaService.creaProposta(new Categoria("Sport"), new ArrayList<>(), new ArrayList<>());
        proposta.putAllValoriCampi(Map.of(
                PropostaService.CAMPO_TITOLO, titolo,
                PropostaService.CAMPO_NUM_PARTECIPANTI, String.valueOf(maxParticipants),
                PropostaService.CAMPO_TERMINE_ISCRIZIONE, termine,
                PropostaService.CAMPO_DATA, dataEvento,
                PropostaService.CAMPO_DATA_CONCLUSIVA, dataEvento,
                PropostaService.CAMPO_ORA, "20:00",
                PropostaService.CAMPO_LUOGO, "Centro sportivo"
        ));

        assertTrue(propostaService.validaProposta(proposta).isEmpty());
        propostaService.pubblicaProposta(proposta);
        return proposta;
    }

    private void setClock(String instant, String zoneId) {
        AppConstants.clock = Clock.fixed(Instant.parse(instant), ZoneId.of(zoneId));
    }
}
