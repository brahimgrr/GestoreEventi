package it.unibs.ingsoft.v2.unit.presentation;

import it.unibs.ingsoft.v2.application.PropostaService;
import it.unibs.ingsoft.v2.domain.AppConstants;
import it.unibs.ingsoft.v2.domain.Campo;
import it.unibs.ingsoft.v2.domain.CampoBaseDefinito;
import it.unibs.ingsoft.v2.domain.Categoria;
import it.unibs.ingsoft.v2.domain.Proposta;
import it.unibs.ingsoft.v2.domain.TipoCampo;
import it.unibs.ingsoft.v2.domain.TipoDato;
import it.unibs.ingsoft.v2.presentation.controller.PropostaController;
import it.unibs.ingsoft.v2.support.InMemoryBachecaRepository;
import it.unibs.ingsoft.v2.support.ScriptedAppView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PropostaControllerTest {

    private PropostaService propostaService;
    private List<Campo> campiBase;
    private List<Campo> campiComuni;
    private Categoria categoriaSport;

    @BeforeEach
    void setUp() {
        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-10T10:00:00Z"), ZoneId.of("UTC"));

        propostaService = new PropostaService(new InMemoryBachecaRepository());
        campiBase = java.util.Arrays.stream(CampoBaseDefinito.values())
                .map(CampoBaseDefinito::toCampo)
                .toList();
        campiComuni = List.of(new Campo("Descrizione", TipoCampo.COMUNE, TipoDato.STRINGA, true));
        categoriaSport = new Categoria("Sport");
        categoriaSport.addCampoSpecifico(new Campo("Attrezzatura", TipoCampo.SPECIFICO, TipoDato.STRINGA, true));
    }

    @Test
    void shouldSaveValidProposalAfterInteractiveCreation() {
        ScriptedAppView view = new ScriptedAppView()
                .addFormResult(validValues("17/01/2025"));

        PropostaController controller = new PropostaController(view, propostaService);
        controller.avviaCreazione(categoriaSport, campiBase, campiComuni);

        assertEquals(1, propostaService.getProposteValide().size());
        assertTrue(view.containsOutput("Proposta valida salvata."));
        assertEquals("Sport Day", view.getShownSummaries().get(0).getValoriCampi().get("Titolo"));
    }

    @Test
    void shouldAllowCorrectionOfInvalidFieldsAndThenSaveProposal() {
        ScriptedAppView view = new ScriptedAppView()
                .addFormResult(invalidValuesRequiringOnlyDataCorrection())
                .addYesNo(true)
                .addFormResult(Map.of("Data", "17/01/2025"));

        PropostaController controller = new PropostaController(view, propostaService);
        controller.avviaCreazione(categoriaSport, campiBase, campiComuni);

        assertEquals(1, propostaService.getProposteValide().size());
        assertEquals("17/01/2025",
                propostaService.getProposteValide().get(0).getValoriCampi().get("Data"));
    }

    @Test
    void shouldDiscardProposalWhenUserRefusesToCorrectErrors() {
        ScriptedAppView view = new ScriptedAppView()
                .addFormResult(validValues("16/01/2025"))
                .addYesNo(false);

        PropostaController controller = new PropostaController(view, propostaService);
        controller.avviaCreazione(categoriaSport, campiBase, campiComuni);

        assertTrue(propostaService.getProposteValide().isEmpty());
        assertTrue(view.containsOutput("Proposta scartata."));
    }

    @Test
    void shouldAbortProposalCreationWhenCorrectionPromptIsCancelled() {
        ScriptedAppView view = new ScriptedAppView()
                .addFormResult(validValues("16/01/2025"))
                .addCancelledYesNo(1);

        PropostaController controller = new PropostaController(view, propostaService);
        controller.avviaCreazione(categoriaSport, campiBase, campiComuni);

        assertTrue(propostaService.getProposteValide().isEmpty());
        assertTrue(view.containsOutput("Operazione annullata."));
    }

    @Test
    void shouldPublishSavedProposalAndRemoveItFromSessionBuffer() {
        Proposta proposta = buildSavedValidProposal();
        ScriptedAppView view = new ScriptedAppView()
                .addIntegers(1)
                .addYesNo(true);

        PropostaController controller = new PropostaController(view, propostaService);
        controller.pubblicaPropostaSalvata();

        assertTrue(propostaService.getProposteValide().isEmpty());
        assertEquals(1, propostaService.getBacheca().size());
        assertEquals(proposta, propostaService.getBacheca().get(0));
        assertTrue(view.containsOutput("Proposta pubblicata in bacheca!"));
    }

    @Test
    void shouldAbortPublicationWhenConfirmationPromptIsCancelled() {
        buildSavedValidProposal();
        ScriptedAppView view = new ScriptedAppView()
                .addIntegers(1)
                .addCancelledYesNo(1);

        PropostaController controller = new PropostaController(view, propostaService);
        controller.pubblicaPropostaSalvata();

        assertEquals(1, propostaService.getProposteValide().size());
        assertTrue(propostaService.getBacheca().isEmpty());
        assertTrue(view.containsOutput("Operazione annullata."));
    }

    @Test
    void shouldShowBachecaGroupedByCategory() {
        Proposta sport = buildSavedValidProposal();
        propostaService.pubblicaProposta(sport);
        propostaService.rimuoviPropostaValida(sport);

        Categoria musica = new Categoria("Musica");
        Proposta musicaProposal = propostaService.creaProposta(musica, campiBase, List.of());
        musicaProposal.putAllValoriCampi(validValues("17/01/2025"));
        updateProposalValues(musicaProposal, "Titolo", "Music Night");
        assertTrue(propostaService.validaProposta(musicaProposal).isEmpty());
        propostaService.pubblicaProposta(musicaProposal);

        ScriptedAppView view = new ScriptedAppView();
        PropostaController controller = new PropostaController(view, propostaService);
        controller.mostraBacheca();

        Map<String, List<Proposta>> bacheca =
                view.getShownBacheche().get(0);
        assertEquals(2, bacheca.size());
        assertEquals(1, bacheca.get("Sport").size());
        assertEquals(1, bacheca.get("Musica").size());
    }

    private Proposta buildSavedValidProposal() {
        Proposta proposta = propostaService.creaProposta(categoriaSport, campiBase, campiComuni);
        proposta.putAllValoriCampi(validValues("17/01/2025"));
        assertTrue(propostaService.validaProposta(proposta).isEmpty());
        propostaService.salvaProposta(proposta);
        return proposta;
    }

    private void updateProposalValues(Proposta proposta, String key, String value) {
        Map<String, String> valori = new LinkedHashMap<>(proposta.getValoriCampi());
        valori.put(key, value);
        proposta.putAllValoriCampi(valori);
    }

    private Map<String, String> validValues(String dataEvento) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("Titolo", "Sport Day");
        values.put("Numero di partecipanti", "10");
        values.put("Termine ultimo di iscrizione", "15/01/2025");
        values.put("Data", dataEvento);
        values.put("Data conclusiva", dataEvento);
        values.put("Ora", "18:00");
        values.put("Luogo", "Stadio");
        values.put("Quota individuale", "12.50");
        values.put("Descrizione", "Evento all'aperto");
        values.put("Attrezzatura", "Pallone");
        return values;
    }

    private Map<String, String> invalidValuesRequiringOnlyDataCorrection() {
        Map<String, String> values = validValues("16/01/2025");
        values.put("Data conclusiva", "17/01/2025");
        return values;
    }
}
