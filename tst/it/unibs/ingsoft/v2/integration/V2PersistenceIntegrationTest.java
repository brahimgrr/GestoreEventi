package it.unibs.ingsoft.v2.integration;

import it.unibs.ingsoft.v2.application.CatalogoService;
import it.unibs.ingsoft.v2.application.PropostaService;
import it.unibs.ingsoft.v2.domain.AppConstants;
import it.unibs.ingsoft.v2.domain.Campo;
import it.unibs.ingsoft.v2.domain.Categoria;
import it.unibs.ingsoft.v2.domain.Proposta;
import it.unibs.ingsoft.v2.persistence.impl.FileBachecaRepository;
import it.unibs.ingsoft.v2.persistence.impl.FileCatalogoRepository;
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

class V2PersistenceIntegrationTest {

    @Test
    void shouldPersistOnlyPublishedOpenProposalsAcrossRestart() {
        AppConstants.clock = Clock.fixed(Instant.parse("2025-01-10T10:00:00Z"), ZoneId.of("UTC"));
        Path testDir = createTestDir();

        CatalogoService catalogoService = new CatalogoService(new FileCatalogoRepository(testDir.resolve("catalogo.json")));
        configureCatalog(catalogoService);

        PropostaService firstRun = new PropostaService(new FileBachecaRepository(testDir.resolve("proposte.json")));
        Categoria sport = catalogoService.getCategorie().get(0);
        List<Campo> campiBase = catalogoService.getCampiBase();
        List<Campo> campiComuni = catalogoService.getCampiComuni();

        Proposta unpublished = createValidatedProposal(firstRun, sport, campiBase, campiComuni, "Bozza valida");
        firstRun.salvaProposta(unpublished);

        Proposta published = createValidatedProposal(firstRun, sport, campiBase, campiComuni, "Pubblicata");
        firstRun.pubblicaProposta(published);

        firstRun.clearProposteValide();

        PropostaService restarted = new PropostaService(new FileBachecaRepository(testDir.resolve("proposte.json")));

        assertEquals(1, restarted.getBacheca().size());
        Proposta recovered = restarted.getBacheca().get(0);
        assertEquals("Pubblicata", recovered.getValoriCampi().get("Titolo"));
        assertEquals(LocalDate.of(2025, 1, 10), recovered.getDataPubblicazione());
        assertEquals(LocalDate.of(2025, 1, 15), recovered.getTermineIscrizione());
        assertEquals(LocalDate.of(2025, 1, 17), recovered.getDataEvento());
    }

    private void configureCatalog(CatalogoService catalogoService) {
        catalogoService.initiateCampiBase();
        catalogoService.addCampoComune("Descrizione", it.unibs.ingsoft.v2.domain.TipoDato.STRINGA, true);
        catalogoService.createCategoria("Sport");
        catalogoService.addCampoSpecifico("Sport", "Attrezzatura", it.unibs.ingsoft.v2.domain.TipoDato.STRINGA, true);
    }

    private Proposta createValidatedProposal(PropostaService propostaService,
                                             Categoria categoria,
                                             List<Campo> campiBase,
                                             List<Campo> campiComuni,
                                             String titolo) {
        Proposta proposta = propostaService.creaProposta(categoria, campiBase, campiComuni);
        proposta.putAllValoriCampi(validValues(titolo));
        assertTrue(propostaService.validaProposta(proposta).isEmpty());
        return proposta;
    }

    private Map<String, String> validValues(String titolo) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("Titolo", titolo);
        values.put("Numero di partecipanti", "10");
        values.put("Termine ultimo di iscrizione", "15/01/2025");
        values.put("Data", "17/01/2025");
        values.put("Data conclusiva", "17/01/2025");
        values.put("Ora", "18:00");
        values.put("Luogo", "Stadio");
        values.put("Quota individuale", "12.50");
        values.put("Descrizione", "Evento all'aperto");
        values.put("Attrezzatura", "Pallone");
        return values;
    }

    private Path createTestDir() {
        try {
            Path dir = Path.of(".codex_build", "v2-persistence-tests", UUID.randomUUID().toString());
            Files.createDirectories(dir);
            return dir;
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile creare la cartella di test.", e);
        }
    }
}
