package it.unibs.ingsoft.v3.application;

import it.unibs.ingsoft.v3.domain.*;
import it.unibs.ingsoft.v3.persistence.api.IBachecaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibs.ingsoft.v3.persistence.api.ISpazioPersonaleRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StateTransitionServiceTest {

    private IBachecaRepository bachecaRepo;
    private NotificationService notificationService;
    private Map<String, SpazioPersonale> mockSpazioDB;
    private StateTransitionService stateTransitionService;
    
    private Bacheca bachecaMock;

    @BeforeEach
    void setUp() {
        bachecaMock = new Bacheca();
        mockSpazioDB = new HashMap<>();
        bachecaRepo = new IBachecaRepository() {
            @Override public Bacheca get() { return bachecaMock; }
            @Override public void save() {}
        };
        
        ISpazioPersonaleRepository spazioRepo = new ISpazioPersonaleRepository() {
            @Override public SpazioPersonale get(String username) {
                return mockSpazioDB.computeIfAbsent(username, k -> new SpazioPersonale());
            }
            @Override public void save() {}
        };
        notificationService = new NotificationService(spazioRepo);
        
        stateTransitionService = new StateTransitionService(bachecaRepo, notificationService);
        
        // Default clock: 2025-01-05
        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-05T10:00:00Z"), ZoneId.of("UTC"));
    }

    private Proposta createProposta(StatoProposta stato, int capacity, String termineIscrizione, String dataConclusiva) {
        Map<String, String> valori = new HashMap<>();
        valori.put(PropostaService.CAMPO_TITOLO, "Test");
        valori.put(PropostaService.CAMPO_NUM_PARTECIPANTI, String.valueOf(capacity));
        
        Proposta p = new Proposta(new Categoria("Cat"), new ArrayList<>(), new ArrayList<>());

        if (termineIscrizione != null) {
            valori.put(PropostaService.CAMPO_TERMINE_ISCRIZIONE, termineIscrizione);
            p.setTermineIscrizione(java.time.LocalDate.parse(termineIscrizione, AppConstants.DATE_FMT));
        }
        if (dataConclusiva != null) {
            valori.put(PropostaService.CAMPO_DATA_CONCLUSIVA, dataConclusiva);
            p.setDataEvento(java.time.LocalDate.parse(dataConclusiva, AppConstants.DATE_FMT));
        }
        
        p.putAllValoriCampi(valori);
        
        // Follow the state machine to reach the desired state
        if (stato != StatoProposta.BOZZA) {
            p.setStato(StatoProposta.VALIDA);
        }
        if (stato == StatoProposta.APERTA || stato == StatoProposta.CONFERMATA || stato == StatoProposta.ANNULLATA || stato == StatoProposta.CONCLUSA) {
            p.setStato(StatoProposta.APERTA);
        }
        if (stato == StatoProposta.CONFERMATA || stato == StatoProposta.CONCLUSA) {
            p.setStato(StatoProposta.CONFERMATA);
        }
        if (stato == StatoProposta.CONCLUSA) {
            p.setStato(StatoProposta.CONCLUSA);
        }
        if (stato == StatoProposta.ANNULLATA) {
            p.setStato(StatoProposta.ANNULLATA);
        }
        
        bachecaMock.addProposta(p);
        return p;
    }

    @Test
    void testControllaScadenze_ApertaNonScaduta_NessunCambio() {
        Proposta p = createProposta(StatoProposta.APERTA, 10, "06/01/2025", null);
        
        stateTransitionService.controllaScadenze();
        
        assertEquals(StatoProposta.APERTA, p.getStato());
        assertTrue(mockSpazioDB.isEmpty());
    }

    @Test
    void testControllaScadenze_ApertaScadutaNonPiena_DiventaAnnullata() {
        Proposta p = createProposta(StatoProposta.APERTA, 10, "04/01/2025", null);
        p.addAderente("mario"); // 1/10
        
        stateTransitionService.controllaScadenze();
        
        assertEquals(StatoProposta.ANNULLATA, p.getStato());
        
        // Verifica notifica nel DB
        assertEquals(1, mockSpazioDB.get("mario").getNotifiche().size());
        assertTrue(mockSpazioDB.get("mario").getNotifiche().get(0).getMessaggio().contains("ANNULLATA"));
    }

    @Test
    void testControllaScadenze_ApertaScadutaPiena_DiventaConfermata() {
        Proposta p = createProposta(StatoProposta.APERTA, 1, "04/01/2025", null);
        p.addAderente("mario"); // 1/1 -> Piena (Anche se in teoria sarebbe già diventata confermata all'iscrizione, testiamo il fallback)
        
        stateTransitionService.controllaScadenze();
        
        assertEquals(StatoProposta.CONFERMATA, p.getStato());
        
        // Verifica notifica nel DB
        assertEquals(1, mockSpazioDB.get("mario").getNotifiche().size());
        assertTrue(mockSpazioDB.get("mario").getNotifiche().get(0).getMessaggio().contains("CONFERMATA"));
    }

    @Test
    void testControllaScadenze_ConfermataNonScaduta_NessunCambio() {
        Proposta p = createProposta(StatoProposta.APERTA, 10, null, "06/01/2025");
        p.addAderente("mario");
        p.setStato(StatoProposta.CONFERMATA);
        
        stateTransitionService.controllaScadenze();
        
        assertEquals(StatoProposta.CONFERMATA, p.getStato());
        
        // No notification should be sent for this state (already confirmed)
        SpazioPersonale spazio = mockSpazioDB.get("mario");
        if (spazio != null) {
            assertTrue(spazio.getNotifiche().isEmpty());
        }
    }

    @Test
    void testControllaScadenze_ConfermataScaduta_DiventaConclusa() {
        Proposta p = createProposta(StatoProposta.CONFERMATA, 10, null, "04/01/2025");
        
        stateTransitionService.controllaScadenze();
        
        assertEquals(StatoProposta.CONCLUSA, p.getStato());
        
        // Nessuna notifica deve essere inviata per la transizione CONCLUSA
        assertTrue(mockSpazioDB.isEmpty() || mockSpazioDB.get("mario") == null || mockSpazioDB.get("mario").getNotifiche().isEmpty());
    }
}
