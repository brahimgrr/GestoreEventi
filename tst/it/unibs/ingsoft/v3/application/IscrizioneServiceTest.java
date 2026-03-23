package it.unibs.ingsoft.v3.application;

import it.unibs.ingsoft.v3.domain.*;
import it.unibs.ingsoft.v3.persistence.api.IBachecaRepository;
import it.unibs.ingsoft.v3.persistence.api.ISpazioPersonaleRepository;
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

import static org.junit.jupiter.api.Assertions.*;

class IscrizioneServiceTest {

    private IBachecaRepository bachecaRepo;
    private NotificationService notificationService;
    private StateTransitionService stateTransitionService;
    private IscrizioneService iscrizioneService;
    
    // Stub di supporto
    private Bacheca bachecaMock;
    private List<Notifica> inviateMock;

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
        iscrizioneService = new IscrizioneService(bachecaRepo, stateTransitionService);
        
        // Fix clock to a specific date for predictable tests (2025-01-01)
        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneId.of("UTC"));
    }

    private Proposta createPropostaAperta(int capacity, String scadenza) {
        Map<String, String> valori = new HashMap<>();
        valori.put(PropostaService.CAMPO_TITOLO, "Test Titolo");
        valori.put(PropostaService.CAMPO_NUM_PARTECIPANTI, String.valueOf(capacity));
        
        Proposta p = new Proposta(new Categoria("Cat"), new ArrayList<>(), new ArrayList<>());
        if (scadenza != null) {
            valori.put(PropostaService.CAMPO_TERMINE_ISCRIZIONE, scadenza);
            p.setTermineIscrizione(java.time.LocalDate.parse(scadenza, AppConstants.DATE_FMT));
        }
        
        p.putAllValoriCampi(valori);
        p.setStato(StatoProposta.VALIDA);
        p.setStato(StatoProposta.APERTA);
        bachecaMock.addProposta(p);
        return p;
    }

    @Test
    void testIscrizione_Successo() {
        Proposta p = createPropostaAperta(10, "02/01/2025");
        Fruitore f = new Fruitore("mario");
        
        assertDoesNotThrow(() -> iscrizioneService.iscrivi(p, f));
        assertTrue(p.getListaAderenti().contains("mario"));
        assertEquals(StatoProposta.APERTA, p.getStato()); // Ancora aperta
    }

    @Test
    void testIscrizione_PropostaNonAperta_LanciaEccezione() {
        Proposta p = new Proposta(new Categoria("Cat"), new ArrayList<>(), new ArrayList<>());
        Fruitore f = new Fruitore("mario");
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> iscrizioneService.iscrivi(p, f));
        assertTrue(ex.getMessage().contains("non è APERTA"));
    }

    private Map<String, SpazioPersonale> mockSpazioDB;

    @Test
    void testIscrizione_Scaduta_LanciaEccezione() {
        Proposta p = createPropostaAperta(10, "31/12/2024"); // Scaduta (Clock = 01/01/2025)
        Fruitore f = new Fruitore("mario");
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> iscrizioneService.iscrivi(p, f));
        assertTrue(ex.getMessage().contains("scaduto"));
    }

    @Test
    void testIscrizione_GiaIscritto_LanciaEccezione() {
        Proposta p = createPropostaAperta(10, "02/01/2025");
        Fruitore f = new Fruitore("mario");
        
        iscrizioneService.iscrivi(p, f);
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> iscrizioneService.iscrivi(p, f));
        assertTrue(ex.getMessage().contains("già iscritto"));
    }

    @Test
    void testIscrizione_CapienzaMassimaRaggiunta_CambioStatoImmediato() {
        Proposta p = createPropostaAperta(2, "02/01/2025");
        Fruitore f1 = new Fruitore("mario");
        Fruitore f2 = new Fruitore("luigi");
        Fruitore f3 = new Fruitore("peach");
        
        iscrizioneService.iscrivi(p, f1);
        assertEquals(StatoProposta.APERTA, p.getStato());
        
        // Iscrizione f2 fa raggiungere max (2) -> diventa CONFERMATA subito
        iscrizioneService.iscrivi(p, f2);
        assertEquals(StatoProposta.CONFERMATA, p.getStato());
        
        // Terza iscrizione deve fallire perché ora non è più APERTA
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> iscrizioneService.iscrivi(p, f3));
        assertTrue(ex.getMessage().contains("non è APERTA"));
    }

    @Test
    void testIscrizione_CapienzaGiaPiena_InTeoriaImpossibileMaDaTestare() {
        Proposta p = createPropostaAperta(1, "02/01/2025");
        p.addAderente("hacker"); // Inserimento manuale fraudolento
        
        Fruitore f = new Fruitore("mario");
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> iscrizioneService.iscrivi(p, f));
        assertTrue(ex.getMessage().contains("raggiunto il numero massimo"));
    }

    @Test
    void testIscrizione_CapienzaCompletata_RifiutaTerzoIscritto() {
        // Create an APERTA proposal with exactly 2 slots
        Proposta p = createPropostaAperta(2, "02/01/2026");
        
        Fruitore f1 = new Fruitore("alice");
        Fruitore f2 = new Fruitore("bob");
        Fruitore f3 = new Fruitore("charlie");
        
        // 1st subscription succeeds
        assertDoesNotThrow(() -> iscrizioneService.iscrivi(p, f1));
        assertEquals(StatoProposta.APERTA, p.getStato());
        assertTrue(p.getListaAderenti().contains("alice"));
        
        // 2nd subscription succeeds and triggers transition to CONFERMATA
        assertDoesNotThrow(() -> iscrizioneService.iscrivi(p, f2));
        assertEquals(StatoProposta.CONFERMATA, p.getStato());
        assertTrue(p.getListaAderenti().contains("bob"));
        assertEquals(2, p.getListaAderenti().size());
        
        // 3rd subscription should throw an exception because it's no longer APERTA 
        // AND because capacity is full (caught by the "non è APERTA" check first)
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> iscrizioneService.iscrivi(p, f3));
        assertTrue(ex.getMessage().contains("non è APERTA") || ex.getMessage().contains("numero massimo"));
        assertFalse(p.getListaAderenti().contains("charlie"));
    }
}
