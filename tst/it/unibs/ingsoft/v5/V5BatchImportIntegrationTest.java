package it.unibs.ingsoft.v5;

import it.unibs.ingsoft.v5.application.CatalogoService;
import it.unibs.ingsoft.v5.application.PropostaService;
import it.unibs.ingsoft.v5.application.batch.BatchImportService;
import it.unibs.ingsoft.v5.application.batch.ImportResult;
import it.unibs.ingsoft.v5.domain.AppConstants;
import it.unibs.ingsoft.v5.domain.Categoria;
import it.unibs.ingsoft.v5.domain.Proposta;
import it.unibs.ingsoft.v5.domain.StatoProposta;
import it.unibs.ingsoft.v5.support.InMemoryBachecaRepository;
import it.unibs.ingsoft.v5.support.InMemoryCatalogoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class V5BatchImportIntegrationTest {

    private Clock originalClock;
    private InMemoryCatalogoRepository catalogoRepository;
    private InMemoryBachecaRepository bachecaRepository;
    private CatalogoService catalogoService;
    private PropostaService propostaService;
    private BatchImportService batchImportService;

    @BeforeEach
    void setUp() {
        originalClock = AppConstants.clock;
        AppConstants.clock = Clock.fixed(Instant.parse("2026-04-01T09:00:00Z"), ZoneId.of("Europe/Rome"));
        catalogoRepository = new InMemoryCatalogoRepository();
        bachecaRepository = new InMemoryBachecaRepository();
        catalogoService = new CatalogoService(catalogoRepository);
        propostaService = new PropostaService(bachecaRepository);
        batchImportService = new BatchImportService(catalogoService, propostaService);
    }

    @AfterEach
    void tearDown() {
        AppConstants.clock = originalClock;
    }

    @Test
    void validBatchImport_populatesCatalogAndCreatesPublishableValidProposals() throws IOException {
        ImportResult result = batchImportService.importa(Path.of("data", "batch_valid.json"));

        assertEquals(6, result.totaleImportati());
        assertFalse(result.hasErrors());
        assertEquals(8, catalogoRepository.getSaveCount());
        assertEquals(0, bachecaRepository.getSaveCount());

        List<Categoria> categorie = catalogoService.getCategorie();
        assertEquals(2, categorie.size());
        assertEquals(2, propostaService.getProposteValide().size());

        Proposta first = propostaService.getProposteValide().get(0);
        assertEquals(StatoProposta.VALIDA, first.getStato());
        assertEquals("Visita al Museo", first.getValoriCampi().get(AppConstants.CAMPO_TITOLO));

        propostaService.pubblicaProposta(first);
        propostaService.rimuoviPropostaValida(first);

        assertEquals(1, bachecaRepository.getSaveCount());
        assertEquals(1, propostaService.getBacheca().size());
        assertEquals(1, propostaService.getProposteValide().size());
    }

    @Test
    void mixedBatchImport_persistsValidSubsetAndLeavesInvalidEntriesOut() throws IOException {
        ImportResult result = batchImportService.importa(Path.of("data", "batch_mixed.json"));

        assertEquals(2, result.totaleImportati());
        assertTrue(result.hasErrors());
        assertEquals(List.of("Musica"), catalogoService.getCategorie().stream().map(Categoria::getNome).toList());
        assertTrue(propostaService.getProposteValide().isEmpty());
        assertTrue(result.getErrori().stream().anyMatch(e -> e.contains("Campo comune")));
        assertTrue(result.getErrori().stream().anyMatch(e -> e.contains("Proposta")));
    }
}
