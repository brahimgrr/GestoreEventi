package it.unibs.ingsoft.v4.application;

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

class IscrizioneServiceTest {

    private Clock originalClock;
    private Bacheca bacheca;
    private int bachecaSaveCount;
    private IscrizioneService iscrizioneService;
    private Map<String, SpazioPersonale> spaziPersonali;

    @BeforeEach
    void setUp() {
        originalClock = AppConstants.clock;
        bacheca = new Bacheca();
        spaziPersonali = new HashMap<>();
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
                return spaziPersonali.computeIfAbsent(username, ignored -> new SpazioPersonale());
            }

            @Override
            public void save() {
            }
        };

        NotificationService notificationService = new NotificationService(spazioRepo);
        StateTransitionService stateTransitionService = new StateTransitionService(bachecaRepo, notificationService);
        iscrizioneService = new IscrizioneService(bachecaRepo, stateTransitionService);

        setClock("2025-01-10T10:00:00Z", "Europe/Rome");
    }

    @AfterEach
    void tearDown() {
        AppConstants.clock = originalClock;
    }

    @Test
    void test_iscrivi_openProposalBeforeDeadline_addsParticipantAndPersists() {
        Proposta proposta = createOpenProposal(3, "11/01/2025", "14/01/2025");

        iscrizioneService.iscrivi(proposta, new Fruitore("alice"));

        assertEquals(StatoProposta.APERTA, proposta.getStato());
        assertEquals(1, proposta.getListaAderenti().size());
        assertEquals("alice", proposta.getListaAderenti().get(0));
        assertEquals(1, bachecaSaveCount);
    }

    @Test
    void test_iscrivi_exactlyAt2359OnDeadline_allowsRegistration() {
        Proposta proposta = createOpenProposal(2, "11/01/2025", "14/01/2025");
        setClock("2025-01-11T22:59:00Z", "Europe/Rome");

        assertDoesNotThrow(() -> iscrizioneService.iscrivi(proposta, new Fruitore("alice")));

        assertEquals(LocalDate.of(2025, 1, 11), LocalDate.now(AppConstants.clock));
        assertEquals(1, proposta.getListaAderenti().size());
        assertTrue(proposta.getListaAderenti().contains("alice"));
    }

    @Test
    void test_iscrivi_at0000AfterDeadline_throwsIllegalStateException() {
        Proposta proposta = createOpenProposal(2, "11/01/2025", "14/01/2025");
        setClock("2025-01-11T23:00:00Z", "Europe/Rome");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> iscrizioneService.iscrivi(proposta, new Fruitore("alice")));

        assertTrue(ex.getMessage().contains("scaduto"));
        assertTrue(proposta.getListaAderenti().isEmpty());
        assertEquals(0, bachecaSaveCount);
    }

    @Test
    void test_iscrivi_whenProposalIsNotOpen_throwsIllegalStateException() {
        Proposta proposta = createOpenProposal(2, "11/01/2025", "14/01/2025");
        proposta.setStato(StatoProposta.CONFERMATA);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> iscrizioneService.iscrivi(proposta, new Fruitore("alice")));

        assertTrue(ex.getMessage().contains("APERTA"));
        assertTrue(proposta.getListaAderenti().isEmpty());
    }

    @Test
    void test_iscrivi_whenAlreadySubscribed_throwsIllegalStateException() {
        Proposta proposta = createOpenProposal(3, "11/01/2025", "14/01/2025");
        Fruitore alice = new Fruitore("alice");
        iscrizioneService.iscrivi(proposta, alice);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> iscrizioneService.iscrivi(proposta, alice));

        assertTrue(ex.getMessage().contains("iscritto"));
        assertEquals(1, proposta.getListaAderenti().size());
    }

    @Test
    void test_iscrivi_whenCapacityIsReached_confirmsProposalAndSendsNotifications() {
        Proposta proposta = createOpenProposal(2, "11/01/2025", "14/01/2025");

        iscrizioneService.iscrivi(proposta, new Fruitore("alice"));
        iscrizioneService.iscrivi(proposta, new Fruitore("bob"));

        assertEquals(StatoProposta.CONFERMATA, proposta.getStato());
        assertEquals(2, proposta.getListaAderenti().size());
        assertEquals(2, spaziPersonali.get("alice").getNotifiche().size() + spaziPersonali.get("bob").getNotifiche().size());
    }

    @Test
    void test_disiscrivi_beforeDeadline_removesParticipantAndPersists() {
        Proposta proposta = createOpenProposal(3, "11/01/2025", "14/01/2025");
        Fruitore alice = new Fruitore("alice");
        iscrizioneService.iscrivi(proposta, alice);
        bachecaSaveCount = 0;

        iscrizioneService.disiscrivi(proposta, alice);

        assertFalse(proposta.getListaAderenti().contains("alice"));
        assertTrue(proposta.getListaAderenti().isEmpty());
        assertEquals(1, bachecaSaveCount);
    }

    @Test
    void test_disiscrivi_exactlyAt2359OnDeadline_allowsCancellation() {
        Proposta proposta = createOpenProposal(3, "11/01/2025", "14/01/2025");
        Fruitore alice = new Fruitore("alice");
        iscrizioneService.iscrivi(proposta, alice);
        setClock("2025-01-11T22:59:00Z", "Europe/Rome");

        assertDoesNotThrow(() -> iscrizioneService.disiscrivi(proposta, alice));

        assertFalse(proposta.getListaAderenti().contains("alice"));
    }

    @Test
    void test_disiscrivi_at0000AfterDeadline_throwsIllegalStateException() {
        Proposta proposta = createOpenProposal(3, "11/01/2025", "14/01/2025");
        Fruitore alice = new Fruitore("alice");
        iscrizioneService.iscrivi(proposta, alice);
        setClock("2025-01-11T23:00:00Z", "Europe/Rome");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> iscrizioneService.disiscrivi(proposta, alice));

        assertTrue(ex.getMessage().contains("scaduto"));
        assertEquals(1, proposta.getListaAderenti().size());
        assertTrue(proposta.getListaAderenti().contains("alice"));
    }

    @Test
    void test_disiscrivi_whenNotSubscribed_throwsIllegalStateException() {
        Proposta proposta = createOpenProposal(3, "11/01/2025", "14/01/2025");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> iscrizioneService.disiscrivi(proposta, new Fruitore("alice")));

        assertTrue(ex.getMessage().contains("Non sei iscritto"));
        assertTrue(proposta.getListaAderenti().isEmpty());
    }

    @Test
    void test_disiscrivi_twice_secondCallFails() {
        Proposta proposta = createOpenProposal(3, "11/01/2025", "14/01/2025");
        Fruitore alice = new Fruitore("alice");
        iscrizioneService.iscrivi(proposta, alice);
        iscrizioneService.disiscrivi(proposta, alice);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> iscrizioneService.disiscrivi(proposta, alice));

        assertTrue(ex.getMessage().contains("Non sei iscritto"));
    }

    @Test
    void test_disiscrivi_whenProposalIsConfirmed_throwsIllegalStateException() {
        Proposta proposta = createOpenProposal(1, "11/01/2025", "14/01/2025");
        Fruitore alice = new Fruitore("alice");
        iscrizioneService.iscrivi(proposta, alice);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> iscrizioneService.disiscrivi(proposta, alice));

        assertEquals(StatoProposta.CONFERMATA, proposta.getStato());
        assertTrue(ex.getMessage().contains("APERTA"));
        assertEquals(List.of("alice"), proposta.getListaAderenti());
    }

    @Test
    void test_iscrivi_whenProposalIsWithdrawn_throwsIllegalStateException() {
        Proposta proposta = createOpenProposal(3, "11/01/2025", "14/01/2025");
        proposta.setStato(StatoProposta.RITIRATA);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> iscrizioneService.iscrivi(proposta, new Fruitore("alice")));

        assertTrue(ex.getMessage().contains("APERTA"));
        assertTrue(proposta.getListaAderenti().isEmpty());
    }

    @Test
    void test_disiscrivi_thenRejoinBeforeDeadline_allowsRejoin() {
        Proposta proposta = createOpenProposal(2, "11/01/2025", "14/01/2025");
        Fruitore alice = new Fruitore("alice");
        iscrizioneService.iscrivi(proposta, alice);

        iscrizioneService.disiscrivi(proposta, alice);
        iscrizioneService.iscrivi(proposta, alice);

        assertEquals(StatoProposta.APERTA, proposta.getStato());
        assertEquals(1, proposta.getListaAderenti().size());
        assertEquals("alice", proposta.getListaAderenti().get(0));
    }

    @Test
    void test_iscrivi_sameInstantWithDifferentClockZone_changesEligibility() {
        Proposta proposta = createOpenProposal(2, "11/01/2025", "14/01/2025");

        setClock("2025-01-11T23:30:00Z", "UTC");
        assertDoesNotThrow(() -> iscrizioneService.iscrivi(proposta, new Fruitore("alice")));
        proposta.removeAderente("alice");

        setClock("2025-01-11T23:30:00Z", "Europe/Rome");
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> iscrizioneService.iscrivi(proposta, new Fruitore("alice")));

        assertTrue(ex.getMessage().contains("scaduto"));
    }

    private Proposta createOpenProposal(int maxPartecipanti, String termineIscrizione, String dataEvento) {
        Proposta proposta = new Proposta(new Categoria("Sport"), new ArrayList<>(), new ArrayList<>());
        proposta.putAllValoriCampi(Map.of(
                PropostaService.CAMPO_TITOLO, "Partita",
                PropostaService.CAMPO_TERMINE_ISCRIZIONE, termineIscrizione,
                PropostaService.CAMPO_DATA, dataEvento,
                PropostaService.CAMPO_DATA_CONCLUSIVA, dataEvento,
                PropostaService.CAMPO_ORA, "18:00",
                PropostaService.CAMPO_LUOGO, "Campo",
                PropostaService.CAMPO_NUM_PARTECIPANTI, String.valueOf(maxPartecipanti)
        ));
        proposta.setTermineIscrizione(LocalDate.parse(termineIscrizione, AppConstants.DATE_FMT));
        proposta.setDataEvento(LocalDate.parse(dataEvento, AppConstants.DATE_FMT));
        proposta.setStato(StatoProposta.VALIDA);
        proposta.setStato(StatoProposta.APERTA);
        bacheca.addProposta(proposta);
        return proposta;
    }

    private void setClock(String instant, String zoneId) {
        AppConstants.clock = Clock.fixed(Instant.parse(instant), ZoneId.of(zoneId));
    }
}
