package it.unibs.ingsoft.v3;

import it.unibs.ingsoft.v3.application.AuthenticationService;
import it.unibs.ingsoft.v3.application.IscrizioneService;
import it.unibs.ingsoft.v3.application.NotificationService;
import it.unibs.ingsoft.v3.application.PropostaService;
import it.unibs.ingsoft.v3.application.StateTransitionService;
import it.unibs.ingsoft.v3.domain.AppConstants;
import it.unibs.ingsoft.v3.domain.Notifica;
import it.unibs.ingsoft.v3.domain.Proposta;
import it.unibs.ingsoft.v3.domain.PropostaStateChange;
import it.unibs.ingsoft.v3.domain.StatoProposta;
import it.unibs.ingsoft.v3.persistence.impl.FileBachecaRepository;
import it.unibs.ingsoft.v3.persistence.impl.FileCredenzialiRepository;
import it.unibs.ingsoft.v3.persistence.impl.FileSpazioPersonaleRepository;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class V3PersistenceIntegrationTest {

    @Test
    void testRestartRecovery_AnnullaPropostaScaduta_PersistendoAderentiENotifiche() {
        Path testDir = createTestDir();
        Fixture firstRun = fixtureAt(testDir, "2025-01-10T10:00:00Z");
        firstRun.authService.registraNuovoFruitore("bob", "pass1");

        Proposta proposta = firstRun.buildAndPublishProposta(3, "11/01/2025", "14/01/2025", "14/01/2025", null);
        firstRun.iscrizioneService.iscrivi(proposta, new it.unibs.ingsoft.v3.domain.Fruitore("bob"));

        Fixture restart = fixtureAt(testDir, "2025-01-12T08:00:00Z");
        restart.stateTransitionService.controllaScadenze();

        Fixture afterRecovery = fixtureAt(testDir, "2025-01-12T08:05:00Z");
        Proposta recovered = afterRecovery.onlyProposal();

        assertEquals(StatoProposta.ANNULLATA, recovered.getStato());
        assertEquals(List.of("bob"), recovered.getListaAderenti());
        assertEquals(1, afterRecovery.notificationService.getNotifiche("bob").size());
        assertTrue(afterRecovery.notificationService.getNotifiche("bob").get(0).getMessaggio().contains("ANNULLATA"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> afterRecovery.iscrizioneService.iscrivi(recovered, new it.unibs.ingsoft.v3.domain.Fruitore("alice")));
        assertTrue(ex.getMessage().contains("APERTA"));
        assertEquals(List.of("bob"), recovered.getListaAderenti());
    }

    @Test
    void testConfermaConQuota_PersistitaConNotificaSoloAiDestinatari() {
        Path testDir = createTestDir();
        Fixture fixture = fixtureAt(testDir, "2025-01-10T10:00:00Z");
        fixture.authService.registraNuovoFruitore("alice", "pass1");
        fixture.authService.registraNuovoFruitore("mallory", "pass1");

        Proposta proposta = fixture.buildAndPublishProposta(1, "15/01/2025", "18/01/2025", "18/01/2025", "25 EUR");
        fixture.iscrizioneService.iscrivi(proposta, new it.unibs.ingsoft.v3.domain.Fruitore("alice"));

        Fixture restarted = fixtureAt(testDir, "2025-01-10T10:30:00Z");
        Proposta persisted = restarted.onlyProposal();
        List<Notifica> notificheAlice = restarted.notificationService.getNotifiche("alice");

        assertEquals(StatoProposta.CONFERMATA, persisted.getStato());
        assertEquals(1, notificheAlice.size());
        assertTrue(notificheAlice.get(0).getMessaggio().contains("25 EUR"));
        assertTrue(notificheAlice.get(0).getMessaggio().contains("18/01/2025"));
        assertTrue(notificheAlice.get(0).getMessaggio().contains("15:00"));
        assertTrue(notificheAlice.get(0).getMessaggio().contains("Stadio"));
        assertTrue(restarted.notificationService.getNotifiche("mallory").isEmpty());
    }

    @Test
    void testStateHistory_PersistitaEAggiornataDopoRiavvioDiPiuGiorni() {
        Path testDir = createTestDir();
        Fixture firstRun = fixtureAt(testDir, "2025-01-10T10:00:00Z");
        firstRun.authService.registraNuovoFruitore("alice", "pass1");

        Proposta proposta = firstRun.buildAndPublishProposta(1, "15/01/2025", "18/01/2025", "18/01/2025", null);
        firstRun.iscrizioneService.iscrivi(proposta, new it.unibs.ingsoft.v3.domain.Fruitore("alice"));

        Fixture restartAfterDays = fixtureAt(testDir, "2025-01-21T09:00:00Z");
        restartAfterDays.stateTransitionService.controllaScadenze();

        Fixture finalRun = fixtureAt(testDir, "2025-01-21T09:05:00Z");
        Proposta concluded = finalRun.onlyProposal();

        assertEquals(StatoProposta.CONCLUSA, concluded.getStato());
        assertEquals(
                List.of(StatoProposta.BOZZA, StatoProposta.VALIDA, StatoProposta.APERTA, StatoProposta.CONFERMATA, StatoProposta.CONCLUSA),
                concluded.getStateHistory().stream().map(PropostaStateChange::getStato).toList()
        );
    }

    @Test
    void testCancellazioneNotifica_RimanePersistitaTraSessioni() {
        Path testDir = createTestDir();
        Fixture firstRun = fixtureAt(testDir, "2025-01-10T10:00:00Z");
        firstRun.authService.registraNuovoFruitore("alice", "pass1");

        Proposta proposta = firstRun.buildAndPublishProposta(1, "15/01/2025", "18/01/2025", "18/01/2025", null);
        firstRun.iscrizioneService.iscrivi(proposta, new it.unibs.ingsoft.v3.domain.Fruitore("alice"));
        Notifica notification = firstRun.notificationService.getNotifiche("alice").get(0);

        firstRun.notificationService.cancellaNotifica("alice", notification);

        Fixture restarted = fixtureAt(testDir, "2025-01-10T11:00:00Z");
        assertTrue(restarted.notificationService.getNotifiche("alice").isEmpty());
    }

    private Fixture fixtureAt(Path testDir, String instant) {
        AppConstants.clock = Clock.fixed(Instant.parse(instant), ZoneId.of("UTC"));

        Path utenti = testDir.resolve("utenti.json");
        Path proposte = testDir.resolve("proposte.json");
        Path notifiche = testDir.resolve("notifiche.json");

        FileCredenzialiRepository credRepo = new FileCredenzialiRepository(utenti);
        FileBachecaRepository bachecaRepo = new FileBachecaRepository(proposte);
        FileSpazioPersonaleRepository spazioRepo = new FileSpazioPersonaleRepository(notifiche);

        AuthenticationService authService = new AuthenticationService(credRepo);
        NotificationService notificationService = new NotificationService(spazioRepo);
        StateTransitionService stateTransitionService = new StateTransitionService(bachecaRepo, notificationService);
        IscrizioneService iscrizioneService = new IscrizioneService(bachecaRepo, stateTransitionService);
        PropostaService propostaService = new PropostaService(bachecaRepo);

        return new Fixture(authService, notificationService, stateTransitionService, iscrizioneService, propostaService, bachecaRepo);
    }

    private Path createTestDir() {
        try {
            Path dir = Path.of(".codex_build", "v3-persistence-tests", UUID.randomUUID().toString());
            Files.createDirectories(dir);
            return dir;
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile creare la cartella di test.", e);
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

        private Proposta buildAndPublishProposta(int capacity, String termine, String dataEvento, String dataConclusiva, String quota) {
            Proposta proposta = propostaService.creaProposta(new it.unibs.ingsoft.v3.domain.Categoria("Sport"), new ArrayList<>(), new ArrayList<>());
            proposta.getValoriCampi().put(PropostaService.CAMPO_TITOLO, "Partita di calcio");
            proposta.getValoriCampi().put(PropostaService.CAMPO_NUM_PARTECIPANTI, String.valueOf(capacity));
            proposta.getValoriCampi().put(PropostaService.CAMPO_TERMINE_ISCRIZIONE, termine);
            proposta.getValoriCampi().put(PropostaService.CAMPO_DATA, dataEvento);
            proposta.getValoriCampi().put(PropostaService.CAMPO_DATA_CONCLUSIVA, dataConclusiva);
            proposta.getValoriCampi().put(PropostaService.CAMPO_ORA, "15:00");
            proposta.getValoriCampi().put(PropostaService.CAMPO_LUOGO, "Stadio");
            if (quota != null) {
                proposta.getValoriCampi().put(PropostaService.CAMPO_QUOTA, quota);
            }

            assertTrue(propostaService.validaProposta(proposta).isEmpty());
            propostaService.pubblicaProposta(proposta);
            return proposta;
        }

        private Proposta onlyProposal() {
            List<Proposta> all = bachecaRepo.get().getProposte();
            assertEquals(1, all.size(), "Attesa una sola proposta persistita.");
            return all.get(0);
        }
    }
}
