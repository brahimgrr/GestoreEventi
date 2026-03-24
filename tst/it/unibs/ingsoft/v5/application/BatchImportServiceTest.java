package it.unibs.ingsoft.v5.application;

import it.unibs.ingsoft.v5.application.batch.BatchImportService;
import it.unibs.ingsoft.v5.application.batch.ImportResult;
import it.unibs.ingsoft.v5.domain.AppConstants;
import it.unibs.ingsoft.v5.domain.Categoria;
import it.unibs.ingsoft.v5.domain.StatoProposta;
import it.unibs.ingsoft.v5.support.InMemoryBachecaRepository;
import it.unibs.ingsoft.v5.support.InMemoryCatalogoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatchImportServiceTest {

    private Clock originalClock;
    private CatalogoService catalogoService;
    private PropostaService propostaService;
    private BatchImportService batchImportService;

    @BeforeEach
    void setUp() {
        originalClock = AppConstants.clock;
        AppConstants.clock = Clock.fixed(Instant.parse("2026-04-01T09:00:00Z"), ZoneId.of("Europe/Rome"));
        catalogoService = new CatalogoService(new InMemoryCatalogoRepository());
        propostaService = new PropostaService(new InMemoryBachecaRepository());
        batchImportService = new BatchImportService(catalogoService, propostaService);
    }

    @AfterEach
    void tearDown() {
        AppConstants.clock = originalClock;
    }

    @Test
    void importa_validFixture_importsAllEntitiesAndCreatesValidProposals() throws IOException {
        ImportResult result = batchImportService.importa(Path.of("data", "batch_valid.json"));

        assertEquals(2, result.getCampiComuniImportati());
        assertEquals(2, result.getCategorieImportate());
        assertEquals(2, result.getProposteImportate());
        assertFalse(result.hasErrors());
        assertEquals(2, propostaService.getProposteValide().size());
        assertTrue(propostaService.getProposteValide().stream().allMatch(p -> p.getStato() == StatoProposta.VALIDA));
        assertTrue(catalogoService.getCategorie().stream().map(Categoria::getNome).toList().containsAll(
                java.util.List.of("Cultura", "Gastronomia")));
    }

    @Test
    void importa_mixedFixture_keepsBestEffortBehavior() throws IOException {
        ImportResult result = batchImportService.importa(Path.of("data", "batch_mixed.json"));

        assertEquals(1, result.getCampiComuniImportati());
        assertEquals(1, result.getCategorieImportate());
        assertEquals(0, result.getProposteImportate());
        assertTrue(result.hasErrors());
        assertTrue(result.getErrori().stream().anyMatch(e -> e.contains("nome vuoto o mancante")));
        assertTrue(result.getErrori().stream().anyMatch(e -> e.contains("tipoDato non valido")));
        assertTrue(result.getErrori().stream().anyMatch(e -> e.contains("categoria 'CategoriaInesistente'")));
        assertEquals(1, catalogoService.getCategorie().size());
        assertEquals("Musica", catalogoService.getCategorie().get(0).getNome());
    }

    @Test
    void importa_rejectsDuplicateProposalInsideSameFile() throws IOException {
        Path file = writeTempJson("""
                {
                  "categorie": [{ "nome": "Cultura", "campiSpecifici": [] }],
                  "proposte": [
                    {
                      "categoria": "Cultura",
                      "valoriCampi": {
                        "Titolo": "Evento Doppio",
                        "Numero di partecipanti": "5",
                        "Termine ultimo di iscrizione": "10/05/2026",
                        "Data": "15/05/2026",
                        "Data conclusiva": "15/05/2026",
                        "Ora": "18:00",
                        "Luogo": "Brescia"
                      }
                    },
                    {
                      "categoria": "Cultura",
                      "valoriCampi": {
                        "Titolo": "evento doppio",
                        "Numero di partecipanti": "6",
                        "Termine ultimo di iscrizione": "10/05/2026",
                        "Data": "15/05/2026",
                        "Data conclusiva": "15/05/2026",
                        "Ora": "18:00",
                        "Luogo": "brescia"
                      }
                    }
                  ]
                }
                """);

        ImportResult result = batchImportService.importa(file);

        assertEquals(1, result.getCategorieImportate());
        assertEquals(1, result.getProposteImportate());
        assertTrue(result.getErrori().stream().anyMatch(e -> e.contains("duplicata nel file di importazione")));
        assertEquals(1, propostaService.getProposteValide().size());
    }

    @Test
    void importa_rejectsDuplicateAgainstExistingSavedProposal() throws IOException {
        PropostaService preloadedService = propostaService;
        var existing = preloadedService.creaProposta(new Categoria("Cultura"), new ArrayList<>(), new ArrayList<>());
        existing.putAllValoriCampi(Map.of(
                PropostaService.CAMPO_TITOLO, "Visita Guidata",
                PropostaService.CAMPO_NUM_PARTECIPANTI, "10",
                PropostaService.CAMPO_TERMINE_ISCRIZIONE, "10/05/2026",
                PropostaService.CAMPO_DATA, "15/05/2026",
                PropostaService.CAMPO_DATA_CONCLUSIVA, "15/05/2026",
                PropostaService.CAMPO_ORA, "09:00",
                PropostaService.CAMPO_LUOGO, "Brescia"
        ));
        assertTrue(preloadedService.validaProposta(existing).isEmpty());
        preloadedService.salvaProposta(existing);
        catalogoService.createCategoria("Cultura");

        Path file = writeTempJson("""
                {
                  "proposte": [
                    {
                      "categoria": "Cultura",
                      "valoriCampi": {
                        "Titolo": "visita guidata",
                        "Numero di partecipanti": "12",
                        "Termine ultimo di iscrizione": "10/05/2026",
                        "Data": "15/05/2026",
                        "Data conclusiva": "15/05/2026",
                        "Ora": "09:00",
                        "Luogo": "brescia"
                      }
                    }
                  ]
                }
                """);

        ImportResult result = batchImportService.importa(file);

        assertEquals(0, result.getProposteImportate());
        assertTrue(result.getErrori().stream().anyMatch(e -> e.contains("stesso Titolo, Data, Ora e Luogo")));
        assertEquals(1, propostaService.getProposteValide().size());
    }

    @Test
    void importa_missingFile_throwsIOException() {
        IOException ex = assertThrows(IOException.class,
                () -> batchImportService.importa(Path.of("data", "missing-import-file.json")));

        assertTrue(ex.getMessage().contains("File non trovato"));
    }

    private Path writeTempJson(String json) throws IOException {
        Path file = Files.createTempFile("gestore-eventi-v5-import-", ".json");
        Files.writeString(file, json);
        file.toFile().deleteOnExit();
        return file;
    }
}
