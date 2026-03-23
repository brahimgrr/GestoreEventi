package it.unibs.ingsoft.v2.unit.application;

import it.unibs.ingsoft.v2.application.PropostaService;
import it.unibs.ingsoft.v2.domain.AppConstants;
import it.unibs.ingsoft.v2.domain.Campo;
import it.unibs.ingsoft.v2.domain.CampoBaseDefinito;
import it.unibs.ingsoft.v2.domain.Categoria;
import it.unibs.ingsoft.v2.domain.Proposta;
import it.unibs.ingsoft.v2.domain.StatoProposta;
import it.unibs.ingsoft.v2.domain.TipoCampo;
import it.unibs.ingsoft.v2.domain.TipoDato;
import it.unibs.ingsoft.v2.support.InMemoryBachecaRepository;
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

    private InMemoryBachecaRepository repo;
    private PropostaService service;
    private List<Campo> campiBase;
    private List<Campo> campiComuni;
    private Categoria categoriaSport;

    @BeforeEach
    void setUp() {
        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-10T10:00:00Z"), ZoneId.of("UTC"));

        repo = new InMemoryBachecaRepository();
        service = new PropostaService(repo);

        campiBase = java.util.Arrays.stream(CampoBaseDefinito.values())
                .map(CampoBaseDefinito::toCampo)
                .toList();
        campiComuni = List.of(new Campo("Descrizione", TipoCampo.COMUNE, TipoDato.STRINGA, true));

        categoriaSport = new Categoria("Sport");
        categoriaSport.addCampoSpecifico(new Campo("Attrezzatura", TipoCampo.SPECIFICO, TipoDato.STRINGA, true));
    }

    @Test
    void shouldCreateDraftProposalForSelectedCategory() {
        Proposta proposta = service.creaProposta(categoriaSport, campiBase, campiComuni);

        assertNotNull(proposta);
        assertEquals(StatoProposta.BOZZA, proposta.getStato());
        assertEquals("Sport", proposta.getCategoria().getNome());
    }

    @Test
    void shouldValidateProposalWhenAllMandatoryFieldsAndDatesAreCorrect() {
        Proposta proposta = buildValidProposal();

        List<String> errori = service.validaProposta(proposta);

        assertTrue(errori.isEmpty(), "Errori inattesi: " + errori);
        assertEquals(StatoProposta.VALIDA, proposta.getStato());
        assertEquals(LocalDate.of(2025, 1, 15), proposta.getTermineIscrizione());
        assertEquals(LocalDate.of(2025, 1, 17), proposta.getDataEvento());
    }

    @Test
    void shouldRejectProposalWithMissingRequiredFields() {
        Proposta proposta = service.creaProposta(categoriaSport, campiBase, campiComuni);
        Map<String, String> valori = validValuesFor("Sport Day", "Pallone");
        valori.remove("Descrizione");
        valori.remove("Attrezzatura");
        proposta.putAllValoriCampi(valori);

        List<String> errori = service.validaProposta(proposta);

        assertEquals(StatoProposta.BOZZA, proposta.getStato());
        assertTrue(errori.stream().anyMatch(e -> e.contains("\"Descrizione\"")));
        assertTrue(errori.stream().anyMatch(e -> e.contains("\"Attrezzatura\"")));
    }

    @Test
    void shouldRejectDeadlineEqualToToday() {
        Proposta proposta = buildValidProposal();
        updateProposalValues(proposta, PropostaService.CAMPO_TERMINE_ISCRIZIONE, "10/01/2025");

        List<String> errori = service.validaProposta(proposta);

        assertEquals(StatoProposta.BOZZA, proposta.getStato());
        assertTrue(errori.stream().anyMatch(e -> e.contains(PropostaService.CAMPO_TERMINE_ISCRIZIONE)));
    }

    @Test
    void shouldRejectEventDateOneDayAfterDeadline() {
        Proposta proposta = buildValidProposal();
        updateProposalValues(proposta, PropostaService.CAMPO_DATA, "16/01/2025");

        List<String> errori = service.validaProposta(proposta);

        assertEquals(StatoProposta.BOZZA, proposta.getStato());
        assertTrue(errori.stream().anyMatch(e -> e.contains("\"Data\"")));
    }

    @Test
    void shouldAcceptEventDateTwoDaysAfterDeadline() {
        Proposta proposta = buildValidProposal();
        updateProposalValues(proposta, PropostaService.CAMPO_DATA, "17/01/2025");

        List<String> errori = service.validaProposta(proposta);

        assertTrue(errori.isEmpty(), "Errori inattesi: " + errori);
        assertEquals(StatoProposta.VALIDA, proposta.getStato());
    }

    @Test
    void shouldRejectInvalidEventDateImmediatelyWhenValidatingSingleField() {
        Proposta proposta = buildValidProposal();
        Map<String, String> valoriCorrenti = new LinkedHashMap<>(proposta.getValoriCampi());
        valoriCorrenti.put(PropostaService.CAMPO_TERMINE_ISCRIZIONE, "15/01/2025");

        List<String> errori = service.validaCampo(
                proposta,
                valoriCorrenti,
                PropostaService.CAMPO_DATA,
                "16/01/2025"
        );

        assertFalse(errori.isEmpty());
        assertTrue(errori.stream().anyMatch(e -> e.contains("\"Data\"")));
    }

    @Test
    void shouldRejectDeadlineImmediatelyWhenItMakesExistingEventDateInvalid() {
        Proposta proposta = buildValidProposal();
        Map<String, String> valoriCorrenti = new LinkedHashMap<>(proposta.getValoriCampi());
        valoriCorrenti.put(PropostaService.CAMPO_DATA, "17/01/2025");

        List<String> errori = service.validaCampo(
                proposta,
                valoriCorrenti,
                PropostaService.CAMPO_TERMINE_ISCRIZIONE,
                "16/01/2025"
        );

        assertFalse(errori.isEmpty());
        assertTrue(errori.stream().anyMatch(e -> e.contains("\"Data\"")));
    }

    @Test
    void shouldRejectConcludingDateBeforeEventDate() {
        Proposta proposta = buildValidProposal();
        updateProposalValues(proposta, PropostaService.CAMPO_DATA_CONCLUSIVA, "16/01/2025");

        List<String> errori = service.validaProposta(proposta);

        assertEquals(StatoProposta.BOZZA, proposta.getStato());
        assertTrue(errori.stream().anyMatch(e -> e.contains(PropostaService.CAMPO_DATA_CONCLUSIVA)));
    }

    @Test
    void shouldRevertValidProposalToDraftWhenRevalidatedAfterBreakingIt() {
        Proposta proposta = buildValidProposal();
        assertTrue(service.validaProposta(proposta).isEmpty());

        updateProposalValues(proposta, PropostaService.CAMPO_DATA, "16/01/2025");
        List<String> errori = service.validaProposta(proposta);

        assertFalse(errori.isEmpty());
        assertEquals(StatoProposta.BOZZA, proposta.getStato());
    }

    @Test
    void shouldSaveOnlyValidProposalsInSessionBuffer() {
        Proposta proposta = buildValidProposal();
        assertTrue(service.validaProposta(proposta).isEmpty());

        service.salvaProposta(proposta);

        assertEquals(1, service.getProposteValide().size());
        assertEquals(0, repo.getSaveCount());
    }

    @Test
    void shouldPublishValidProposalAndPersistOpenState() {
        Proposta proposta = buildValidProposal();
        assertTrue(service.validaProposta(proposta).isEmpty());

        service.pubblicaProposta(proposta);

        assertEquals(StatoProposta.APERTA, proposta.getStato());
        assertEquals(LocalDate.of(2025, 1, 10), proposta.getDataPubblicazione());
        assertEquals(1, repo.getSaveCount());
        assertEquals(1, repo.get().getProposte().size());
    }

    @Test
    void shouldRejectPublishingInvalidOrAlreadyOpenProposal() {
        Proposta proposta = buildValidProposal();

        assertThrows(IllegalStateException.class, () -> service.pubblicaProposta(proposta));

        assertTrue(service.validaProposta(proposta).isEmpty());
        service.pubblicaProposta(proposta);

        assertThrows(IllegalStateException.class, () -> service.pubblicaProposta(proposta));
    }

    @Test
    void shouldRejectDuplicateOpenProposal() {
        Proposta first = buildValidProposal();
        assertTrue(service.validaProposta(first).isEmpty());
        service.pubblicaProposta(first);

        Proposta duplicate = buildValidProposal();
        assertTrue(service.validaProposta(duplicate).isEmpty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.pubblicaProposta(duplicate)
        );

        assertTrue(exception.getMessage().contains("stesso"));
    }

    @Test
    void shouldGroupBachecaByCategoryAndReturnOnlyOpenProposals() {
        Proposta sport = buildValidProposal();
        assertTrue(service.validaProposta(sport).isEmpty());
        service.pubblicaProposta(sport);

        Categoria musica = new Categoria("Musica");
        Proposta openMusic = new Proposta(musica, new ArrayList<>(), new ArrayList<>());
        openMusic.setStato(StatoProposta.VALIDA);
        openMusic.setStato(StatoProposta.APERTA);
        repo.get().addProposta(openMusic);

        Proposta validButClosed = new Proposta(new Categoria("Arte"), new ArrayList<>(), new ArrayList<>());
        validButClosed.setStato(StatoProposta.VALIDA);
        repo.get().addProposta(validButClosed);

        Map<String, List<Proposta>> grouped = service.getBachecaPerCategoria();

        assertEquals(2, grouped.size());
        assertEquals(1, grouped.get("Sport").size());
        assertEquals(1, grouped.get("Musica").size());
        assertFalse(grouped.containsKey("Arte"));
    }

    @Test
    void shouldMapErrorsOnlyToQuotedFieldNames() {
        Proposta proposta = buildValidProposal();
        List<String> errori = List.of("Campo obbligatorio mancante: \"" + PropostaService.CAMPO_DATA_CONCLUSIVA + "\".");
        List<String> nomi = service.getCampiConErrore(proposta, errori).stream()
                .map(Campo::getNome)
                .toList();

        assertEquals(List.of(PropostaService.CAMPO_DATA_CONCLUSIVA), nomi);
    }

    private Proposta buildValidProposal() {
        Proposta proposta = service.creaProposta(categoriaSport, campiBase, campiComuni);
        proposta.putAllValoriCampi(validValuesFor("Sport Day", "Pallone"));
        return proposta;
    }

    private void updateProposalValues(Proposta proposta, String key, String value) {
        Map<String, String> valori = new LinkedHashMap<>(proposta.getValoriCampi());
        valori.put(key, value);
        proposta.putAllValoriCampi(valori);
    }

    private Map<String, String> validValuesFor(String titolo, String attrezzatura) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put(PropostaService.CAMPO_TITOLO, titolo);
        values.put(PropostaService.CAMPO_NUM_PARTECIPANTI, "10");
        values.put(PropostaService.CAMPO_TERMINE_ISCRIZIONE, "15/01/2025");
        values.put(PropostaService.CAMPO_DATA, "17/01/2025");
        values.put(PropostaService.CAMPO_DATA_CONCLUSIVA, "17/01/2025");
        values.put(PropostaService.CAMPO_ORA, "18:00");
        values.put(PropostaService.CAMPO_LUOGO, "Stadio");
        values.put(PropostaService.CAMPO_QUOTA, "12.50");
        values.put("Descrizione", "Evento all'aperto");
        values.put("Attrezzatura", attrezzatura);
        return values;
    }
}
