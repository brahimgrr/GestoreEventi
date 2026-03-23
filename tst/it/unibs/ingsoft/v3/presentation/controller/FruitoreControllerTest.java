package it.unibs.ingsoft.v3.presentation.controller;

import it.unibs.ingsoft.v3.application.IscrizioneService;
import it.unibs.ingsoft.v3.application.NotificationService;
import it.unibs.ingsoft.v3.application.PropostaService;
import it.unibs.ingsoft.v3.application.StateTransitionService;
import it.unibs.ingsoft.v3.domain.AppConstants;
import it.unibs.ingsoft.v3.domain.Bacheca;
import it.unibs.ingsoft.v3.domain.Categoria;
import it.unibs.ingsoft.v3.domain.Fruitore;
import it.unibs.ingsoft.v3.domain.Notifica;
import it.unibs.ingsoft.v3.domain.Proposta;
import it.unibs.ingsoft.v3.domain.SpazioPersonale;
import it.unibs.ingsoft.v3.persistence.api.IBachecaRepository;
import it.unibs.ingsoft.v3.persistence.api.ISpazioPersonaleRepository;
import it.unibs.ingsoft.v3.support.ScriptedAppView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FruitoreControllerTest {

    private Proposta proposta;
    private PropostaService propostaService;
    private NotificationService notificationService;
    private IscrizioneService iscrizioneService;
    private Map<String, SpazioPersonale> spazi;

    @BeforeEach
    void setUp() {
        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-10T10:00:00Z"), ZoneId.of("UTC"));

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
        StateTransitionService stateTransitionService = new StateTransitionService(bachecaRepo, notificationService);
        iscrizioneService = new IscrizioneService(bachecaRepo, stateTransitionService);

        proposta = propostaService.creaProposta(new Categoria("Sport"), new ArrayList<>(), new ArrayList<>());
        proposta.putAllValoriCampi(Map.of(
                PropostaService.CAMPO_TITOLO, "Partita",
                PropostaService.CAMPO_NUM_PARTECIPANTI, "3",
                PropostaService.CAMPO_TERMINE_ISCRIZIONE, "15/01/2025",
                PropostaService.CAMPO_DATA, "18/01/2025",
                PropostaService.CAMPO_DATA_CONCLUSIVA, "18/01/2025",
                PropostaService.CAMPO_ORA, "15:00",
                PropostaService.CAMPO_LUOGO, "Stadio"
        ));
        assertTrue(propostaService.validaProposta(proposta).isEmpty());
        propostaService.pubblicaProposta(proposta);
    }

    @Test
    void testRun_IscriveSoloIlFruitoreLoggato() {
        ScriptedAppView view = new ScriptedAppView()
                .addIntegers(1, 1, 0)
                .addYesNo(true);

        Fruitore loggedFruitore = new Fruitore("alice");
        FruitoreController controller = new FruitoreController(
                loggedFruitore,
                view,
                propostaService,
                iscrizioneService,
                new SpazioPersonaleController(loggedFruitore, view, notificationService)
        );

        controller.run();

        assertEquals(1, proposta.getListaAderenti().size());
        assertEquals("alice", proposta.getListaAderenti().get(0));
        assertFalse(proposta.getListaAderenti().contains("bob"));
        assertTrue(view.containsOutput("Iscrizione effettuata con successo"));
    }

    @Test
    void testSpazioPersonaleController_MostraSoloLeNotificheDelFruitoreCorrente() {
        notificationService.inviaNotifica("alice", new Notifica("notifica alice"));
        notificationService.inviaNotifica("bob", new Notifica("notifica bob"));

        ScriptedAppView view = new ScriptedAppView().addIntegers(0);
        SpazioPersonaleController controller = new SpazioPersonaleController(new Fruitore("alice"), view, notificationService);

        controller.run();

        String rendered = String.join("\n", view.getOutputs());
        assertTrue(rendered.contains("notifica alice"));
        assertFalse(rendered.contains("notifica bob"));
    }
}
