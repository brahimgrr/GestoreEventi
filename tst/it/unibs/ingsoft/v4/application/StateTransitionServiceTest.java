package it.unibs.ingsoft.v4.application;

import it.unibs.ingsoft.v4.domain.AppConstants;
import it.unibs.ingsoft.v4.domain.Bacheca;
import it.unibs.ingsoft.v4.domain.Categoria;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StateTransitionServiceTest {

    private Clock originalClock;
    private Bacheca bacheca;
    private Map<String, SpazioPersonale> spazi;
    private int bachecaSaveCount;
    private StateTransitionService stateTransitionService;

    @BeforeEach
    void setUp() {
        originalClock = AppConstants.clock;
        bacheca = new Bacheca();
        spazi = new HashMap<>();
        bachecaSaveCount = 0;

        IBachecaRepository bachecaRepo = new IBachecaRepository() {
            @Override
            public Bacheca get() {
                return bacheca;
            }

            @Override
            public void save() {
                bachecaSaveCount++;
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

        stateTransitionService = new StateTransitionService(bachecaRepo, new NotificationService(spazioRepo));
        setClock("2025-01-11T12:00:00Z", "Europe/Rome");
    }

    @AfterEach
    void tearDown() {
        AppConstants.clock = originalClock;
    }

    @Test
    void test_ritiraProposta_openProposalBeforeEventDay_setsWithdrawnNotifiesAllAndPersists() {
        Proposta proposta = createProposalInState(StatoProposta.APERTA, "15/01/2025", List.of("alice", "bob"));

        stateTransitionService.ritiraProposta(proposta);

        assertEquals(StatoProposta.RITIRATA, proposta.getStato());
        assertEquals(List.of("alice", "bob"), proposta.getListaAderenti());
        assertEquals(1, notificationMessages("alice").size());
        assertEquals(1, notificationMessages("bob").size());
        assertTrue(notificationMessages("alice").get(0).contains("RITIRATA"));
        assertEquals(1, bachecaSaveCount);
    }

    @Test
    void test_ritiraProposta_confirmedProposalBeforeEventDay_setsWithdrawn() {
        Proposta proposta = createProposalInState(StatoProposta.CONFERMATA, "15/01/2025", List.of("alice"));

        stateTransitionService.ritiraProposta(proposta);

        assertEquals(StatoProposta.RITIRATA, proposta.getStato());
        assertEquals(List.of("alice"), proposta.getListaAderenti());
        assertEquals(2, spazi.get("alice").getNotifiche().size());
        assertTrue(spazi.get("alice").getNotifiche().get(1).getMessaggio().contains("RITIRATA"));
    }

    @Test
    void test_ritiraProposta_exactlyAt2359DayBeforeEvent_allowsWithdrawal() {
        Proposta proposta = createProposalInState(StatoProposta.APERTA, "12/01/2025", List.of("alice"));
        setClock("2025-01-11T22:59:00Z", "Europe/Rome");

        assertDoesNotThrow(() -> stateTransitionService.ritiraProposta(proposta));

        assertEquals(StatoProposta.RITIRATA, proposta.getStato());
    }

    @Test
    void test_ritiraProposta_at0000OnEventDay_throwsIllegalStateException() {
        Proposta proposta = createProposalInState(StatoProposta.APERTA, "12/01/2025", List.of("alice"));
        setClock("2025-01-11T23:00:00Z", "Europe/Rome");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> stateTransitionService.ritiraProposta(proposta));

        assertTrue(ex.getMessage().contains("giorno precedente"));
        assertEquals(StatoProposta.APERTA, proposta.getStato());
        assertEquals(0, notificationMessages("alice").size());
    }

    @Test
    void test_ritiraProposta_whenProposalIsNotOpenOrConfirmed_throwsIllegalStateException() {
        Proposta proposta = createProposalInState(StatoProposta.BOZZA, "15/01/2025", List.of());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> stateTransitionService.ritiraProposta(proposta));

        assertTrue(ex.getMessage().contains("non"));
        assertEquals(StatoProposta.BOZZA, proposta.getStato());
    }

    @Test
    void test_ritiraProposta_notifiesOnlyCurrentParticipants() {
        Proposta proposta = createProposalInState(StatoProposta.APERTA, "15/01/2025", List.of("alice", "bob"));

        stateTransitionService.ritiraProposta(proposta);

        assertEquals(1, notificationMessages("alice").size());
        assertEquals(1, notificationMessages("bob").size());
        assertFalse(spazi.containsKey("mallory"));
    }

    @Test
    void test_ritiraProposta_recordsWithdrawalInStateHistoryWithClockDate() {
        Proposta proposta = createProposalInState(StatoProposta.APERTA, "15/01/2025", List.of("alice"));

        stateTransitionService.ritiraProposta(proposta);

        PropostaStateChange last = proposta.getStateHistory().get(proposta.getStateHistory().size() - 1);
        assertEquals(StatoProposta.RITIRATA, last.getStato());
        assertEquals(LocalDate.of(2025, 1, 11), last.getDataCambio());
    }

    @Test
    void test_ritiraProposta_withoutParticipants_setsWithdrawnWithoutCreatingNotifications() {
        Proposta proposta = createProposalInState(StatoProposta.APERTA, "15/01/2025", List.of());

        stateTransitionService.ritiraProposta(proposta);

        assertEquals(StatoProposta.RITIRATA, proposta.getStato());
        assertTrue(spazi.isEmpty());
    }

    @Test
    void test_ritiraProposta_twice_secondCallFailsWithoutDuplicatingHistoryOrNotifications() {
        Proposta proposta = createProposalInState(StatoProposta.APERTA, "15/01/2025", List.of("alice"));

        stateTransitionService.ritiraProposta(proposta);
        int historySize = proposta.getStateHistory().size();
        int notifications = spazi.get("alice").getNotifiche().size();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> stateTransitionService.ritiraProposta(proposta));

        assertTrue(ex.getMessage().contains("APERTA") || ex.getMessage().contains("CONFERMATA"));
        assertEquals(historySize, proposta.getStateHistory().size());
        assertEquals(notifications, spazi.get("alice").getNotifiche().size());
    }

    @Test
    void test_controllaScadenze_withWithdrawnProposal_isIdempotentAndDoesNotSendNotifications() {
        Proposta proposta = createProposalInState(StatoProposta.APERTA, "15/01/2025", List.of("alice"));
        stateTransitionService.ritiraProposta(proposta);
        bachecaSaveCount = 0;
        int historySize = proposta.getStateHistory().size();
        int notificationCount = spazi.get("alice").getNotifiche().size();

        stateTransitionService.controllaScadenze();

        assertEquals(StatoProposta.RITIRATA, proposta.getStato());
        assertEquals(historySize, proposta.getStateHistory().size());
        assertEquals(notificationCount, spazi.get("alice").getNotifiche().size());
        assertEquals(0, bachecaSaveCount);
    }

    private Proposta createProposalInState(StatoProposta stato, String dataEvento, List<String> aderenti) {
        Proposta proposta = new Proposta(new Categoria("Cultura"), new ArrayList<>(), new ArrayList<>());
        proposta.putAllValoriCampi(Map.of(
                PropostaService.CAMPO_TITOLO, "Museo",
                PropostaService.CAMPO_TERMINE_ISCRIZIONE, "10/01/2025",
                PropostaService.CAMPO_DATA, dataEvento,
                PropostaService.CAMPO_DATA_CONCLUSIVA, dataEvento,
                PropostaService.CAMPO_ORA, "09:00",
                PropostaService.CAMPO_LUOGO, "Centro",
                PropostaService.CAMPO_NUM_PARTECIPANTI, "4"
        ));
        proposta.setTermineIscrizione(LocalDate.parse("10/01/2025", AppConstants.DATE_FMT));
        proposta.setDataEvento(LocalDate.parse(dataEvento, AppConstants.DATE_FMT));
        if (stato != StatoProposta.BOZZA) {
            proposta.setStato(StatoProposta.VALIDA);
        }
        if (stato == StatoProposta.APERTA || stato == StatoProposta.CONFERMATA) {
            proposta.setStato(StatoProposta.APERTA);
            for (String aderente : aderenti) {
                proposta.addAderente(aderente);
            }
        }
        if (stato == StatoProposta.CONFERMATA) {
            stateTransitionService.confermaProposta(proposta);
        }
        bacheca.addProposta(proposta);
        return proposta;
    }

    private List<String> notificationMessages(String username) {
        return spazi.getOrDefault(username, new SpazioPersonale()).getNotifiche().stream()
                .map(Notifica::getMessaggio)
                .toList();
    }

    private void setClock(String instant, String zoneId) {
        AppConstants.clock = Clock.fixed(Instant.parse(instant), ZoneId.of(zoneId));
    }
}
