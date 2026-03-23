package it.unibs.ingsoft.v2.integration;

import it.unibs.ingsoft.v2.application.CatalogoService;
import it.unibs.ingsoft.v2.application.PropostaService;
import it.unibs.ingsoft.v2.domain.AppConstants;
import it.unibs.ingsoft.v2.domain.Campo;
import it.unibs.ingsoft.v2.domain.Categoria;
import it.unibs.ingsoft.v2.domain.Proposta;
import it.unibs.ingsoft.v2.persistence.impl.FileBachecaRepository;
import it.unibs.ingsoft.v2.persistence.impl.FileCatalogoRepository;
import it.unibs.ingsoft.v2.presentation.controller.PropostaController;
import it.unibs.ingsoft.v2.support.ScriptedAppView;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class V2WorkflowIntegrationTest {

    @Test
    void shouldCreatePublishAndShowOpenProposalsGroupedByCategory() {
        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-10T10:00:00Z"), ZoneId.of("UTC"));
        Path testDir = createTestDir();

        CatalogoService catalogoService = new CatalogoService(new FileCatalogoRepository(testDir.resolve("catalogo.json")));
        PropostaService propostaService = new PropostaService(new FileBachecaRepository(testDir.resolve("proposte.json")));

        configureCatalog(catalogoService);
        Categoria sport = catalogoService.getCategorie().stream().filter(c -> c.getNome().equals("Sport")).findFirst().orElseThrow();
        Categoria musica = catalogoService.getCategorie().stream().filter(c -> c.getNome().equals("Musica")).findFirst().orElseThrow();
        List<Campo> campiBase = catalogoService.getCampiBase();
        List<Campo> campiComuni = catalogoService.getCampiComuni();

        new PropostaController(new ScriptedAppView().addFormResult(validValues("Sport Day", "Sport", "Pallone")), propostaService)
                .avviaCreazione(sport, campiBase, campiComuni);
        new PropostaController(new ScriptedAppView().addFormResult(validValues("Music Night", "Musica", "Chitarra")), propostaService)
                .avviaCreazione(musica, campiBase, campiComuni);

        PropostaController publishController = new PropostaController(
                new ScriptedAppView().addIntegers(1, 1).addYesNo(true, true),
                propostaService
        );
        publishController.pubblicaPropostaSalvata();
        publishController.pubblicaPropostaSalvata();

        ScriptedAppView showView = new ScriptedAppView();
        new PropostaController(showView, propostaService).mostraBacheca();

        Map<String, List<Proposta>> shown = showView.getShownBacheche().get(0);
        assertEquals(2, shown.size());
        assertEquals(1, shown.get("Sport").size());
        assertEquals(1, shown.get("Musica").size());
        assertEquals(2, propostaService.getBacheca().size());

        PropostaService restarted = new PropostaService(new FileBachecaRepository(testDir.resolve("proposte.json")));
        assertEquals(2, restarted.getBacheca().size());
        assertTrue(restarted.getBacheca().stream().allMatch(p -> p.getDataPubblicazione().equals(LocalDate.of(2025, 1, 10))));
    }

    private void configureCatalog(CatalogoService catalogoService) {
        catalogoService.initiateCampiBase();
        catalogoService.addCampoComune("Descrizione", it.unibs.ingsoft.v2.domain.TipoDato.STRINGA, true);
        catalogoService.createCategoria("Sport");
        catalogoService.createCategoria("Musica");
        catalogoService.addCampoSpecifico("Sport", "Attrezzatura", it.unibs.ingsoft.v2.domain.TipoDato.STRINGA, true);
        catalogoService.addCampoSpecifico("Musica", "Strumento", it.unibs.ingsoft.v2.domain.TipoDato.STRINGA, true);
    }

    private Map<String, String> validValues(String titolo, String categoria, String specificoValue) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("Titolo", titolo);
        values.put("Numero di partecipanti", "10");
        values.put("Termine ultimo di iscrizione", "15/01/2025");
        values.put("Data", "17/01/2025");
        values.put("Data conclusiva", "17/01/2025");
        values.put("Ora", "18:00");
        values.put("Luogo", categoria + " Hall");
        values.put("Quota individuale", "12.50");
        values.put("Descrizione", "Evento " + categoria);
        values.put(categoria.equals("Sport") ? "Attrezzatura" : "Strumento", specificoValue);
        return values;
    }

    private Path createTestDir() {
        try {
            Path dir = Path.of(".codex_build", "v2-workflow-tests", UUID.randomUUID().toString());
            Files.createDirectories(dir);
            return dir;
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile creare la cartella di test.", e);
        }
    }
}
