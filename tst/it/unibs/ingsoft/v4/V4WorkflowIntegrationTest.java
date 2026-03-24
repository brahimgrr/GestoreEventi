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

class V4WorkflowIntegrationTest {

    private Clock originalClock;
    private PropostaService propostaService;
    private StateTransitionService stateTransitionService;
    private IscrizioneService iscrizioneService;
    private NotificationService notificationService;

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
    void test_openToConfirmedToWithdrawn_multipleUsers_receiveOnlyExpectedNotifications_andParticipantsBecomeImmutable() {
        Proposta proposta = buildAndPublishProposal(2, "15/01/2025", "20/01/2025", "Gita al museo");
        Fruitore alice = new Fruitore("alice");
        Fruitore bob = new Fruitore("bob");

        iscrizioneService.iscrivi(proposta, alice);
        iscrizioneService.iscrivi(proposta, bob);

        assertEquals(StatoProposta.CONFERMATA, proposta.getStato());
        assertEquals(1, messages("alice").stream().filter(m -> m.contains("CONFERMATA")).count());
        assertEquals(1, messages("bob").stream().filter(m -> m.contains("CONFERMATA")).count());

        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-19T12:00:00Z"), ZoneId.of("Europe/Rome"));
        stateTransitionService.ritiraProposta(proposta);

        assertEquals(StatoProposta.RITIRATA, proposta.getStato());
        assertEquals(List.of("alice", "bob"), proposta.getListaAderenti());
        assertEquals(2, notificationService.getNotifiche("alice").size());
        assertEquals(2, notificationService.getNotifiche("bob").size());
        assertTrue(messages("alice").get(1).contains("RITIRATA"));
        assertTrue(messages("bob").get(1).contains("RITIRATA"));
        assertTrue(notificationService.getNotifiche("mallory").isEmpty());

        IllegalStateException joinEx = assertThrows(IllegalStateException.class,
                () -> iscrizioneService.iscrivi(proposta, new Fruitore("charlie")));
        IllegalStateException leaveEx = assertThrows(IllegalStateException.class,
                () -> iscrizioneService.disiscrivi(proposta, alice));
        IllegalStateException addEx = assertThrows(IllegalStateException.class,
                () -> proposta.addAderente("charlie"));

        assertTrue(joinEx.getMessage().contains("APERTA"));
        assertTrue(leaveEx.getMessage().contains("APERTA"));
        assertTrue(addEx.getMessage().contains("APERTA") || addEx.getMessage().contains("non"));
        assertEquals(List.of("alice", "bob"), proposta.getListaAderenti());
    }

    @Test
    void test_withdrawOpenProposal_removesItFromPublicBoard_butKeepsItPersistedInAllProposals() {
        Proposta proposta = buildAndPublishProposal(3, "15/01/2025", "20/01/2025", "Escursione");
        iscrizioneService.iscrivi(proposta, new Fruitore("alice"));

        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-19T12:00:00Z"), ZoneId.of("Europe/Rome"));
        stateTransitionService.ritiraProposta(proposta);

        assertEquals(StatoProposta.RITIRATA, proposta.getStato());
        assertFalse(propostaService.getBacheca().contains(proposta));
        assertTrue(propostaService.getTutteLeProposte().contains(proposta));
        assertEquals(1, notificationService.getNotifiche("alice").size());
        assertTrue(notificationService.getNotifiche("alice").get(0).getMessaggio().contains("RITIRATA"));
    }

    private Proposta buildAndPublishProposal(int maxParticipants, String termine, String dataEvento, String titolo) {
        Proposta proposta = propostaService.creaProposta(new Categoria("Cultura"), new ArrayList<>(), new ArrayList<>());
        proposta.putAllValoriCampi(Map.of(
                PropostaService.CAMPO_TITOLO, titolo,
                PropostaService.CAMPO_NUM_PARTECIPANTI, String.valueOf(maxParticipants),
                PropostaService.CAMPO_TERMINE_ISCRIZIONE, termine,
                PropostaService.CAMPO_DATA, dataEvento,
                PropostaService.CAMPO_DATA_CONCLUSIVA, dataEvento,
                PropostaService.CAMPO_ORA, "10:00",
                PropostaService.CAMPO_LUOGO, "Brescia"
        ));

        assertTrue(propostaService.validaProposta(proposta).isEmpty());
        propostaService.pubblicaProposta(proposta);
        return proposta;
    }

    private List<String> messages(String username) {
        return notificationService.getNotifiche(username).stream().map(Notifica::getMessaggio).toList();
    }
}
