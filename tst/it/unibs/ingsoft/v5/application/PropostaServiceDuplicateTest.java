package it.unibs.ingsoft.v5.application;

import it.unibs.ingsoft.v5.domain.AppConstants;
import it.unibs.ingsoft.v5.domain.Categoria;
import it.unibs.ingsoft.v5.domain.Proposta;
import it.unibs.ingsoft.v5.support.InMemoryBachecaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropostaServiceDuplicateTest {

    private Clock originalClock;
    private PropostaService service;

    @BeforeEach
    void setUp() {
        originalClock = AppConstants.clock;
        AppConstants.clock = Clock.fixed(Instant.parse("2026-04-01T09:00:00Z"), ZoneId.of("Europe/Rome"));
        service = new PropostaService(new InMemoryBachecaRepository());
    }

    @AfterEach
    void tearDown() {
        AppConstants.clock = originalClock;
    }

    @Test
    void salvaProposta_rejectsDuplicateAgainstSavedValidProposal_caseInsensitive() {
        Proposta first = createValidProposal("Visita al Museo", "15/05/2026", "10:00", "Brescia");
        Proposta duplicate = createValidProposal("  visita al museo ", "15/05/2026", "10:00", " brescia ");

        service.salvaProposta(first);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.salvaProposta(duplicate));

        assertTrue(ex.getMessage().contains("stesso Titolo, Data, Ora e Luogo"));
        assertEquals(1, service.getProposteValide().size());
    }

    @Test
    void salvaProposta_rejectsDuplicateAgainstPublishedProposal() {
        Proposta published = createValidProposal("Escursione", "18/05/2026", "09:30", "Lago");
        service.pubblicaProposta(published);

        Proposta duplicate = createValidProposal("escursione", "18/05/2026", "09:30", "lago");
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.salvaProposta(duplicate));

        assertTrue(ex.getMessage().contains("stesso Titolo, Data, Ora e Luogo"));
    }

    @Test
    void chiaveIdentita_normalizesWhitespaceAndCase() {
        String key = Proposta.chiaveIdentita(Map.of(
                AppConstants.CAMPO_TITOLO, "  Concerto Jazz  ",
                AppConstants.CAMPO_DATA, "20/05/2026",
                AppConstants.CAMPO_ORA, "21:00",
                AppConstants.CAMPO_LUOGO, " Teatro Grande "
        ));

        assertEquals("concerto jazz|20/05/2026|21:00|teatro grande", key);
    }

    private Proposta createValidProposal(String titolo, String dataEvento, String ora, String luogo) {
        Proposta proposta = service.creaProposta(new Categoria("Cultura"), new ArrayList<>(), new ArrayList<>());
        proposta.putAllValoriCampi(Map.of(
                PropostaService.CAMPO_TITOLO, titolo,
                PropostaService.CAMPO_NUM_PARTECIPANTI, "10",
                PropostaService.CAMPO_TERMINE_ISCRIZIONE, "10/05/2026",
                PropostaService.CAMPO_DATA, dataEvento,
                PropostaService.CAMPO_DATA_CONCLUSIVA, dataEvento,
                PropostaService.CAMPO_ORA, ora,
                PropostaService.CAMPO_LUOGO, luogo
        ));
        assertTrue(service.validaProposta(proposta).isEmpty());
        return proposta;
    }
}
