package it.unibs.ingsoft.v4;

import it.unibs.ingsoft.v4.application.AuthenticationService;
import it.unibs.ingsoft.v4.application.IscrizioneService;
import it.unibs.ingsoft.v4.application.NotificationService;
import it.unibs.ingsoft.v4.application.PropostaService;
import it.unibs.ingsoft.v4.application.StateTransitionService;
import it.unibs.ingsoft.v4.domain.AppConstants;
import it.unibs.ingsoft.v4.domain.Categoria;
import it.unibs.ingsoft.v4.domain.Fruitore;
import it.unibs.ingsoft.v4.domain.Notifica;
import it.unibs.ingsoft.v4.domain.Proposta;
import it.unibs.ingsoft.v4.domain.PropostaStateChange;
import it.unibs.ingsoft.v4.domain.StatoProposta;
import it.unibs.ingsoft.v4.persistence.impl.FileBachecaRepository;
import it.unibs.ingsoft.v4.persistence.impl.FileCredenzialiRepository;
import it.unibs.ingsoft.v4.persistence.impl.FileSpazioPersonaleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class V4PersistenceIntegrationTest {

    private Clock originalClock = AppConstants.clock;

    @AfterEach
    void tearDown() {
        AppConstants.clock = originalClock;
    }

    @Test
    void test_withdrawnOpenProposal_persistsStateWithdrawalDateFrozenParticipantsAndBlocksChangesAfterRestart() {
        Path testDir = createTestDir();
        Fixture firstRun = fixtureAt(testDir, "2025-01-10T09:00:00Z");
        firstRun.authService.registraNuovoFruitore("alice", "pass1");
        firstRun.authService.registraNuovoFruitore("bob", "pass1");
        firstRun.authService.registraNuovoFruitore("charlie", "pass1");

        Proposta proposta = firstRun.buildAndPublishProposal(3, "15/01/2025", "18/01/2025", "Visita guidata");
        firstRun.iscrizioneService.iscrivi(proposta, new Fruitore("alice"));
        firstRun.iscrizioneService.iscrivi(proposta, new Fruitore("bob"));

        Fixture withdrawalRun = fixtureAt(testDir, "2025-01-17T12:00:00Z");
        withdrawalRun.stateTransitionService.ritiraProposta(withdrawalRun.onlyProposal());

        Fixture restarted = fixtureAt(testDir, "2025-01-17T12:05:00Z");
        Proposta persisted = restarted.onlyProposal();

        assertEquals(StatoProposta.RITIRATA, persisted.getStato());
        assertEquals(List.of("alice", "bob"), persisted.getListaAderenti());
        PropostaStateChange last = persisted.getStateHistory().get(persisted.getStateHistory().size() - 1);
        assertEquals(StatoProposta.RITIRATA, last.getStato());
        assertEquals(LocalDate.of(2025, 1, 17), last.getDataCambio());
        assertEquals(1, restarted.notificationService.getNotifiche("alice").size());
        assertEquals(1, restarted.notificationService.getNotifiche("bob").size());
        assertTrue(restarted.notificationService.getNotifiche("charlie").isEmpty());
        assertTrue(restarted.propostaService.getBacheca().isEmpty());

        IllegalStateException joinEx = assertThrows(IllegalStateException.class,
                () -> restarted.iscrizioneService.iscrivi(persisted, new Fruitore("charlie")));
        IllegalStateException leaveEx = assertThrows(IllegalStateException.class,
                () -> restarted.iscrizioneService.disiscrivi(persisted, new Fruitore("alice")));
        IllegalStateException addEx = assertThrows(IllegalStateException.class,
                () -> persisted.addAderente("charlie"));
        IllegalStateException removeEx = assertThrows(IllegalStateException.class,
                () -> persisted.removeAderente("alice"));

        assertTrue(joinEx.getMessage().contains("APERTA"));
        assertTrue(leaveEx.getMessage().contains("APERTA"));
        assertTrue(addEx.getMessage().contains("APERTA") || addEx.getMessage().contains("non"));
        assertTrue(removeEx.getMessage().contains("APERTA") || removeEx.getMessage().contains("non"));
        assertEquals(List.of("alice", "bob"), persisted.getListaAderenti());
    }

    @Test
    void test_withdrawnConfirmedProposal_persistsFinalParticipantListAndNotifiesOnlyParticipants() {
        Path testDir = createTestDir();
        Fixture firstRun = fixtureAt(testDir, "2025-01-10T09:00:00Z");
        firstRun.authService.registraNuovoFruitore("alice", "pass1");
        firstRun.authService.registraNuovoFruitore("bob", "pass1");
        firstRun.authService.registraNuovoFruitore("mallory", "pass1");

        Proposta proposta = firstRun.buildAndPublishProposal(2, "15/01/2025", "18/01/2025", "Laboratorio");
        firstRun.iscrizioneService.iscrivi(proposta, new Fruitore("alice"));
        firstRun.iscrizioneService.iscrivi(proposta, new Fruitore("bob"));

        Fixture withdrawalRun = fixtureAt(testDir, "2025-01-17T12:00:00Z");
        withdrawalRun.stateTransitionService.ritiraProposta(withdrawalRun.onlyProposal());

        Fixture restarted = fixtureAt(testDir, "2025-01-17T12:05:00Z");
        Proposta persisted = restarted.onlyProposal();
        List<Notifica> aliceNotifications = restarted.notificationService.getNotifiche("alice");
        List<Notifica> bobNotifications = restarted.notificationService.getNotifiche("bob");

        assertEquals(StatoProposta.RITIRATA, persisted.getStato());
        assertEquals(List.of("alice", "bob"), persisted.getListaAderenti());
        assertEquals(2, aliceNotifications.size());
        assertEquals(2, bobNotifications.size());
        assertTrue(aliceNotifications.get(0).getMessaggio().contains("CONFERMATA"));
        assertTrue(aliceNotifications.get(1).getMessaggio().contains("RITIRATA"));
        assertTrue(bobNotifications.get(0).getMessaggio().contains("CONFERMATA"));
        assertTrue(bobNotifications.get(1).getMessaggio().contains("RITIRATA"));
        assertTrue(restarted.notificationService.getNotifiche("mallory").isEmpty());
    }

    @Test
    void test_cancelledUser_beforeWithdrawal_isNotPersistedInFrozenParticipantList_andGetsNoNotificationAfterRestart() {
        Path testDir = createTestDir();
        Fixture firstRun = fixtureAt(testDir, "2025-01-10T09:00:00Z");
        firstRun.authService.registraNuovoFruitore("alice", "pass1");
        firstRun.authService.registraNuovoFruitore("bob", "pass1");
        firstRun.authService.registraNuovoFruitore("charlie", "pass1");

        Proposta proposta = firstRun.buildAndPublishProposal(4, "15/01/2025", "18/01/2025", "Workshop");
        firstRun.iscrizioneService.iscrivi(proposta, new Fruitore("alice"));
        firstRun.iscrizioneService.iscrivi(proposta, new Fruitore("bob"));
        firstRun.iscrizioneService.disiscrivi(proposta, new Fruitore("bob"));
        firstRun.iscrizioneService.iscrivi(proposta, new Fruitore("charlie"));

        Fixture withdrawalRun = fixtureAt(testDir, "2025-01-17T12:00:00Z");
        withdrawalRun.stateTransitionService.ritiraProposta(withdrawalRun.onlyProposal());

        Fixture restarted = fixtureAt(testDir, "2025-01-17T12:05:00Z");
        Proposta persisted = restarted.onlyProposal();

        assertEquals(StatoProposta.RITIRATA, persisted.getStato());
        assertEquals(List.of("alice", "charlie"), persisted.getListaAderenti());
        assertEquals(1, restarted.notificationService.getNotifiche("alice").size());
        assertEquals(1, restarted.notificationService.getNotifiche("charlie").size());
        assertTrue(restarted.notificationService.getNotifiche("bob").isEmpty());
    }

    @Test
    void test_withdrawnProposal_afterMultipleRestarts_remainsWithdrawnInvisibleAndImmutable() {
        Path testDir = createTestDir();
        Fixture firstRun = fixtureAt(testDir, "2025-01-10T09:00:00Z");
        firstRun.authService.registraNuovoFruitore("alice", "pass1");

        Proposta proposta = firstRun.buildAndPublishProposal(2, "15/01/2025", "18/01/2025", "Concerto");
        firstRun.iscrizioneService.iscrivi(proposta, new Fruitore("alice"));

        Fixture withdrawalRun = fixtureAt(testDir, "2025-01-17T12:00:00Z");
        withdrawalRun.stateTransitionService.ritiraProposta(withdrawalRun.onlyProposal());

        Fixture secondRestart = fixtureAt(testDir, "2025-01-18T08:00:00Z");
        secondRestart.stateTransitionService.controllaScadenze();

        Fixture thirdRestart = fixtureAt(testDir, "2025-01-19T08:00:00Z");
        Proposta persisted = thirdRestart.onlyProposal();

        assertEquals(StatoProposta.RITIRATA, persisted.getStato());
        assertTrue(thirdRestart.propostaService.getBacheca().isEmpty());
        assertEquals(List.of("alice"), persisted.getListaAderenti());
        assertEquals(1, thirdRestart.notificationService.getNotifiche("alice").size());
        assertThrows(IllegalStateException.class,
                () -> thirdRestart.iscrizioneService.disiscrivi(persisted, new Fruitore("alice")));
    }

    private Fixture fixtureAt(Path testDir, String instant) {
        AppConstants.clock = Clock.fixed(Instant.parse(instant), ZoneId.of("Europe/Rome"));

        FileCredenzialiRepository credRepo = new FileCredenzialiRepository(testDir.resolve("utenti.json"));
        FileBachecaRepository bachecaRepo = new FileBachecaRepository(testDir.resolve("proposte.json"));
        FileSpazioPersonaleRepository spazioRepo = new FileSpazioPersonaleRepository(testDir.resolve("notifiche.json"));

        AuthenticationService authService = new AuthenticationService(credRepo);
        NotificationService notificationService = new NotificationService(spazioRepo);
        StateTransitionService stateTransitionService = new StateTransitionService(bachecaRepo, notificationService);
        IscrizioneService iscrizioneService = new IscrizioneService(bachecaRepo, stateTransitionService);
        PropostaService propostaService = new PropostaService(bachecaRepo);

        return new Fixture(authService, notificationService, stateTransitionService, iscrizioneService, propostaService, bachecaRepo);
    }

    private Path createTestDir() {
        try {
            Path dir = Path.of(".codex_build", "v4-persistence-tests", UUID.randomUUID().toString());
            Files.createDirectories(dir);
            return dir;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create test directory.", e);
        }
    }

    private static final class Fixture {
        private final AuthenticationService authService;
        private final NotificationService notificationService;
        private final StateTransitionService stateTransitionService;
        private final IscrizioneService iscrizioneService;
        private final PropostaService propostaService;
        private final FileBachecaRepository bachecaRepo;

        private Fixture(AuthenticationService authService,
                        NotificationService notificationService,
                        StateTransitionService stateTransitionService,
                        IscrizioneService iscrizioneService,
                        PropostaService propostaService,
                        FileBachecaRepository bachecaRepo) {
            this.authService = authService;
            this.notificationService = notificationService;
            this.stateTransitionService = stateTransitionService;
            this.iscrizioneService = iscrizioneService;
            this.propostaService = propostaService;
            this.bachecaRepo = bachecaRepo;
        }

        private Proposta buildAndPublishProposal(int maxParticipants, String termine, String dataEvento, String titolo) {
            Proposta proposta = propostaService.creaProposta(new Categoria("Cultura"), new ArrayList<>(), new ArrayList<>());
            proposta.putAllValoriCampi(Map.of(
                    PropostaService.CAMPO_TITOLO, titolo,
                    PropostaService.CAMPO_NUM_PARTECIPANTI, String.valueOf(maxParticipants),
                    PropostaService.CAMPO_TERMINE_ISCRIZIONE, termine,
                    PropostaService.CAMPO_DATA, dataEvento,
                    PropostaService.CAMPO_DATA_CONCLUSIVA, dataEvento,
                    PropostaService.CAMPO_ORA, "09:30",
                    PropostaService.CAMPO_LUOGO, "Brescia"
            ));

            assertTrue(propostaService.validaProposta(proposta).isEmpty());
            propostaService.pubblicaProposta(proposta);
            return proposta;
        }

        private Proposta onlyProposal() {
            List<Proposta> all = bachecaRepo.get().getProposte();
            assertEquals(1, all.size(), "Expected exactly one persisted proposal.");
            return all.get(0);
        }
    }
}
