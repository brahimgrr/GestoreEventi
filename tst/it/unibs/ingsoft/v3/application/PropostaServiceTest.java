package it.unibs.ingsoft.v3.application;

import it.unibs.ingsoft.v3.domain.*;
import it.unibs.ingsoft.v3.persistence.api.IBachecaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PropostaServiceTest {

    private PropostaService service;
    private Bacheca bachecaMock;

    @BeforeEach
    void setUp() {
        // Fix clock: today = 10/01/2025
        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-10T10:00:00Z"), ZoneId.of("UTC"));

        bachecaMock = new Bacheca();
        IBachecaRepository bachecaRepo = new IBachecaRepository() {
            @Override public Bacheca get() { return bachecaMock; }
            @Override public void save() {}
        };

        service = new PropostaService(bachecaRepo);
    }

    // -----------------------------------------------------------------
    // Helper: returns a Proposta whose valoriCampi pass validaProposta
    // with clock fixed at 10/01/2025.
    // -----------------------------------------------------------------
    private Proposta buildValidProposal() {
        Proposta p = service.creaProposta(new Categoria("Sport"), new ArrayList<>(), new ArrayList<>());
        p.putAllValoriCampi(Map.of(
                PropostaService.CAMPO_TITOLO, "Partita di calcio",
                PropostaService.CAMPO_TERMINE_ISCRIZIONE, "15/01/2025",
                PropostaService.CAMPO_DATA, "18/01/2025",
                PropostaService.CAMPO_DATA_CONCLUSIVA, "18/01/2025",
                PropostaService.CAMPO_NUM_PARTECIPANTI, "5",
                PropostaService.CAMPO_ORA, "15:00",
                PropostaService.CAMPO_LUOGO, "Stadio"
        ));
        return p;
    }

    // =================================================================
    // GROUP A — creaProposta
    // =================================================================

    @Test
    void testCreaProposta_CreaInStatoBozza() {
        Proposta p = service.creaProposta(new Categoria("Sport"), new ArrayList<>(), new ArrayList<>());
        assertNotNull(p);
        assertEquals(StatoProposta.BOZZA, p.getStato());
    }

    @Test
    void testCreaProposta_CategoriaNull_LanciaEccezione() {
        assertThrows(IllegalArgumentException.class,
                () -> service.creaProposta(null, new ArrayList<>(), new ArrayList<>()));
    }

    // =================================================================
    // GROUP B — static date validators
    // =================================================================

    @Test
    void testIsTermineIscrizioneValido_DataFutura_ReturnsTrue() {
        LocalDate tomorrow = LocalDate.now(AppConstants.clock).plusDays(1);
        assertTrue(PropostaService.isTermineIscrizioneValido(tomorrow));
    }

    @Test
    void testIsTermineIscrizioneValido_DataOggi_ReturnsFalse() {
        LocalDate today = LocalDate.now(AppConstants.clock);
        assertFalse(PropostaService.isTermineIscrizioneValido(today));
    }

    @Test
    void testIsTermineIscrizioneValido_DataPassata_ReturnsFalse() {
        LocalDate yesterday = LocalDate.now(AppConstants.clock).minusDays(1);
        assertFalse(PropostaService.isTermineIscrizioneValido(yesterday));
    }

    @Test
    void testIsDataEventoValida_DueGiorniDopo_ReturnsTrue() {
        LocalDate termine = LocalDate.of(2025, 1, 15);
        LocalDate data = termine.plusDays(2); // 17/01/2025 — isAfter(termine.plusDays(1)) == true
        assertTrue(PropostaService.isDataEventoValida(data, termine));
    }

    @Test
    void testIsDataEventoValida_UnGiornoDopo_ReturnsFalse() {
        LocalDate termine = LocalDate.of(2025, 1, 15);
        LocalDate data = termine.plusDays(1); // 16/01/2025 — isAfter(16/01) is false when equal
        assertFalse(PropostaService.isDataEventoValida(data, termine));
    }

    @Test
    void testIsDataConclusivaValida_StessoGiorno_ReturnsTrue() {
        LocalDate data = LocalDate.of(2025, 1, 18);
        assertTrue(PropostaService.isDataConclusivaValida(data, data));
    }

    @Test
    void testIsDataConclusivaValida_PrimaDellaData_ReturnsFalse() {
        LocalDate data = LocalDate.of(2025, 1, 18);
        assertFalse(PropostaService.isDataConclusivaValida(data.minusDays(1), data));
    }

    // =================================================================
    // GROUP C — validaProposta
    // =================================================================

    @Test
    void testValidaProposta_TuttiCampiValidi_ReturnsListaVuota() {
        Proposta p = buildValidProposal();
        List<String> errori = service.validaProposta(p);
        assertTrue(errori.isEmpty(), "Expected no errors but got: " + errori);
        assertEquals(StatoProposta.VALIDA, p.getStato());
    }

    @Test
    void testValidaProposta_TermineScaduto_ReturnsErrore() {
        Proposta p = buildValidProposal();
        updateProposalValues(p, PropostaService.CAMPO_TERMINE_ISCRIZIONE, "09/01/2025");
        List<String> errori = service.validaProposta(p);
        assertFalse(errori.isEmpty());
        assertEquals(StatoProposta.BOZZA, p.getStato());
    }

    @Test
    void testValidaProposta_DataEventoTroppoVicina_ReturnsErrore() {
        Proposta p = buildValidProposal();
        // termine = 15/01, data = 16/01 (only 1 day gap — must be > 1 day)
        updateProposalValues(p, PropostaService.CAMPO_DATA, "16/01/2025");
        List<String> errori = service.validaProposta(p);
        assertFalse(errori.isEmpty());
        assertEquals(StatoProposta.BOZZA, p.getStato());
    }

    @Test
    void testValidaProposta_DataConclusivaAntecedente_ReturnsErrore() {
        Proposta p = buildValidProposal();
        // data evento = 18/01, conclusiva = 17/01 (before event)
        updateProposalValues(p, PropostaService.CAMPO_DATA_CONCLUSIVA, "17/01/2025");
        List<String> errori = service.validaProposta(p);
        assertFalse(errori.isEmpty());
        assertEquals(StatoProposta.BOZZA, p.getStato());
    }

    @Test
    void testValidaCampo_DataEventoTroppoVicina_ReturnsErroreImmediato() {
        Proposta p = buildValidProposal();

        List<String> errori = service.validaCampo(
                p,
                Map.of(PropostaService.CAMPO_TERMINE_ISCRIZIONE, "15/01/2025"),
                PropostaService.CAMPO_DATA,
                "16/01/2025"
        );

        assertFalse(errori.isEmpty());
        assertTrue(errori.stream().anyMatch(e -> e.contains("\"Data\"")));
    }

    @Test
    void testValidaCampo_TermineRendeDataEsistenteNonValida_ReturnsErroreImmediato() {
        Proposta p = buildValidProposal();

        List<String> errori = service.validaCampo(
                p,
                Map.of(PropostaService.CAMPO_DATA, "18/01/2025"),
                PropostaService.CAMPO_TERMINE_ISCRIZIONE,
                "17/01/2025"
        );

        assertFalse(errori.isEmpty());
        assertTrue(errori.stream().anyMatch(e -> e.contains("\"Data\"")));
    }

    @Test
    void testValidaProposta_Idempotente_DoubleCall_StaysValid() {
        Proposta p = buildValidProposal();

        // First validation
        List<String> firstErrors = service.validaProposta(p);
        assertTrue(firstErrors.isEmpty());
        assertEquals(StatoProposta.VALIDA, p.getStato());

        // Second validation on the same VALIDA proposal (should revert to BOZZA then re-advance)
        List<String> secondErrors = service.validaProposta(p);
        assertTrue(secondErrors.isEmpty(), "Second validation failed: " + secondErrors);
        assertEquals(StatoProposta.VALIDA, p.getStato());
    }

    // =================================================================
    // GROUP D — pubblicaProposta
    // =================================================================

    @Test
    void testPubblicaProposta_PropostaValida_DiventaAperta() {
        Proposta p = buildValidProposal();
        service.validaProposta(p);

        service.pubblicaProposta(p);

        assertEquals(StatoProposta.APERTA, p.getStato());
        assertNotNull(p.getDataPubblicazione());
        assertTrue(service.getBacheca().contains(p));
    }

    @Test
    void testPubblicaProposta_PropostaNonValida_LanciaEccezione() {
        Proposta p = buildValidProposal(); // still BOZZA, not validated
        assertThrows(IllegalStateException.class, () -> service.pubblicaProposta(p));
    }

    @Test
    void testPubblicaProposta_TermineScaduto_LanciaEccezione() {
        Proposta p = buildValidProposal();
        service.validaProposta(p); // → VALIDA

        // Manually set termineIscrizione to a past date on the Proposta object
        p.setTermineIscrizione(LocalDate.of(2025, 1, 9)); // yesterday relative to fixed clock

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.pubblicaProposta(p));
        assertTrue(ex.getMessage().contains("termine"));
    }

    @Test
    void testPubblicaProposta_Duplicato_LanciaEccezione() {
        // Publish proposal A
        Proposta a = buildValidProposal();
        service.validaProposta(a);
        service.pubblicaProposta(a);

        // Build identical proposal B (same titolo, data, ora, luogo)
        Proposta b = buildValidProposal();
        service.validaProposta(b);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.pubblicaProposta(b));
        assertTrue(ex.getMessage().toLowerCase().contains("duplica") ||
                   ex.getMessage().contains("stesso"));
    }

    // =================================================================
    // GROUP E — bacheca queries
    // =================================================================

    @Test
    void testGetBacheca_RestituiscesoloAPERTE() {
        // APERTA proposal
        Proposta aperta = buildValidProposal();
        service.validaProposta(aperta);
        service.pubblicaProposta(aperta); // → APERTA, added to bacheca

        // CONFERMATA proposal (walk state manually — not via PropostaService to avoid dup check)
        Proposta confermata = new Proposta(new Categoria("Musica"), new ArrayList<>(), new ArrayList<>());
        confermata.setStato(StatoProposta.VALIDA);
        confermata.setStato(StatoProposta.APERTA);
        confermata.setStato(StatoProposta.CONFERMATA);
        bachecaMock.addProposta(confermata);

        // ANNULLATA proposal
        Proposta annullata = new Proposta(new Categoria("Arte"), new ArrayList<>(), new ArrayList<>());
        annullata.setStato(StatoProposta.VALIDA);
        annullata.setStato(StatoProposta.APERTA);
        annullata.setStato(StatoProposta.ANNULLATA);
        bachecaMock.addProposta(annullata);

        List<Proposta> bacheca = service.getBacheca();
        assertEquals(1, bacheca.size());
        assertEquals(StatoProposta.APERTA, bacheca.get(0).getStato());
    }

    @Test
    void testGetBachecaPerCategoria_GruppaPerNomeCategoria() {
        // Two APERTA Sport proposals
        Proposta sport1 = new Proposta(new Categoria("Sport"), new ArrayList<>(), new ArrayList<>());
        sport1.putAllValoriCampi(Map.of(
                PropostaService.CAMPO_TITOLO, "Sport1",
                PropostaService.CAMPO_DATA, "20/01/2025",
                PropostaService.CAMPO_ORA, "10:00",
                PropostaService.CAMPO_LUOGO, "Campo A"
        ));
        sport1.setStato(StatoProposta.VALIDA);
        sport1.setStato(StatoProposta.APERTA);
        bachecaMock.addProposta(sport1);

        Proposta sport2 = new Proposta(new Categoria("Sport"), new ArrayList<>(), new ArrayList<>());
        sport2.putAllValoriCampi(Map.of(
                PropostaService.CAMPO_TITOLO, "Sport2",
                PropostaService.CAMPO_DATA, "21/01/2025",
                PropostaService.CAMPO_ORA, "11:00",
                PropostaService.CAMPO_LUOGO, "Campo B"
        ));
        sport2.setStato(StatoProposta.VALIDA);
        sport2.setStato(StatoProposta.APERTA);
        bachecaMock.addProposta(sport2);

        // One APERTA Musica proposal
        Proposta musica = new Proposta(new Categoria("Musica"), new ArrayList<>(), new ArrayList<>());
        musica.putAllValoriCampi(Map.of(
                PropostaService.CAMPO_TITOLO, "Concerto",
                PropostaService.CAMPO_DATA, "22/01/2025",
                PropostaService.CAMPO_ORA, "20:00",
                PropostaService.CAMPO_LUOGO, "Teatro"
        ));
        musica.setStato(StatoProposta.VALIDA);
        musica.setStato(StatoProposta.APERTA);
        bachecaMock.addProposta(musica);

        Map<String, List<Proposta>> perCategoria = service.getBachecaPerCategoria();

        assertEquals(2, perCategoria.size());
        assertTrue(perCategoria.containsKey("Sport"));
        assertTrue(perCategoria.containsKey("Musica"));
        assertEquals(2, perCategoria.get("Sport").size());
        assertEquals(1, perCategoria.get("Musica").size());
    }

    // =================================================================
    // GROUP F — session-scoped valid proposals
    // =================================================================

    @Test
    void testSalvaProposta_StatoBozza_LanciaEccezione() {
        Proposta p = buildValidProposal(); // still BOZZA
        assertThrows(IllegalStateException.class, () -> service.salvaProposta(p));
    }

    @Test
    void testClearProposteValide_SvuotaLaLista() {
        Proposta p1 = buildValidProposal();
        service.validaProposta(p1);
        service.salvaProposta(p1);

        Proposta p2 = new Proposta(new Categoria("Arte"), new ArrayList<>(), new ArrayList<>());
        p2.putAllValoriCampi(Map.of(
                PropostaService.CAMPO_TERMINE_ISCRIZIONE, "15/01/2025",
                PropostaService.CAMPO_DATA, "18/01/2025",
                PropostaService.CAMPO_DATA_CONCLUSIVA, "18/01/2025"
        ));
        service.validaProposta(p2);
        service.salvaProposta(p2);

        assertEquals(2, service.getProposteValide().size());

        service.clearProposteValide();

        assertTrue(service.getProposteValide().isEmpty());
    }

    private void updateProposalValues(Proposta proposta, String key, String value) {
        Map<String, String> valori = new LinkedHashMap<>(proposta.getValoriCampi());
        valori.put(key, value);
        proposta.putAllValoriCampi(valori);
    }
}
