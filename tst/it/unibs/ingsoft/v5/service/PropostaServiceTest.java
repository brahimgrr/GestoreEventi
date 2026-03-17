package it.unibs.ingsoft.v5.service;

import it.unibs.ingsoft.v5.model.*;
import it.unibs.ingsoft.v5.persistence.AppData;
import it.unibs.ingsoft.v5.persistence.IPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V5 – PropostaService")
class PropostaServiceTest
{
    private AppData data;
    private PropostaService ps;
    private CategoriaService cs;

    @BeforeEach
    void setUp()
    {
        data = new AppData();
        IPersistenceService mockDb = new IPersistenceService()
        {
            @Override public AppData loadOrCreate() { return data; }
            @Override public void save(AppData d) {}
        };
        cs = new CategoriaService(mockDb, data);
        ps = new PropostaService(mockDb, data);
        cs.createCategoria("Sport");
    }

    // ───────────────────── creaProposta ─────────────────────

    @Test
    @DisplayName("creaProposta with valid category → state is BOZZA")
    void creaProposta_valid()
    {
        Proposta p = ps.creaProposta("Sport");
        assertEquals(StatoProposta.BOZZA, p.getStato());
        assertEquals("Sport", p.getCategoria().getNome());
    }

    @Test
    @DisplayName("creaProposta with non-existing category → throws IllegalArgumentException")
    void creaProposta_nonExisting_throws()
    {
        assertThrows(IllegalArgumentException.class, () -> ps.creaProposta("Inesistente"));
    }

    // ───────────────────── validaProposta ─────────────────────

    @Test
    @DisplayName("validaProposta with all valid fields and dates → no errors, state becomes VALIDA")
    void validaProposta_allValid_noErrors()
    {
        Proposta p = ps.creaProposta("Sport");
        LocalDate deadline  = LocalDate.now().plusDays(5);
        LocalDate eventDate = deadline.plusDays(3);

        p.putAllValoriCampi(Map.of(
                "Titolo",                       "Gita",
                "Numero di partecipanti",       "10",
                "Termine ultimo di iscrizione", deadline.format(AppConstants.DATE_FMT),
                "Luogo",                        "Brescia",
                "Data",                         eventDate.format(AppConstants.DATE_FMT),
                "Ora",                          "09:00",
                "Quota individuale",            "25.00",
                "Data conclusiva",              eventDate.format(AppConstants.DATE_FMT)
        ));

        List<String> errori = ps.validaProposta(p);
        assertTrue(errori.isEmpty(), "Expected no errors but got: " + errori);
        assertEquals(StatoProposta.VALIDA, p.getStato());
    }

    @Test
    @DisplayName("validaProposta with past inscription deadline → error returned")
    void validaProposta_pastDeadline_hasError()
    {
        Proposta p = ps.creaProposta("Sport");
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate eventDate = LocalDate.now().plusDays(10);

        p.putAllValoriCampi(Map.of(
                "Titolo",                       "Gita",
                "Numero di partecipanti",       "10",
                "Termine ultimo di iscrizione", yesterday.format(AppConstants.DATE_FMT),
                "Luogo",                        "Brescia",
                "Data",                         eventDate.format(AppConstants.DATE_FMT),
                "Ora",                          "09:00",
                "Quota individuale",            "25.00",
                "Data conclusiva",              eventDate.format(AppConstants.DATE_FMT)
        ));

        List<String> errori = ps.validaProposta(p);
        assertFalse(errori.isEmpty());
        assertTrue(errori.stream().anyMatch(e -> e.contains("Termine ultimo")));
        assertEquals(StatoProposta.BOZZA, p.getStato());
    }

    @Test
    @DisplayName("validaProposta with event date only 1 day after deadline → error returned")
    void validaProposta_eventTooClose_hasError()
    {
        Proposta p = ps.creaProposta("Sport");
        LocalDate deadline  = LocalDate.now().plusDays(5);
        LocalDate eventDate = deadline.plusDays(1);   // needs at least +2

        p.putAllValoriCampi(Map.of(
                "Titolo",                       "Gita",
                "Numero di partecipanti",       "10",
                "Termine ultimo di iscrizione", deadline.format(AppConstants.DATE_FMT),
                "Luogo",                        "Brescia",
                "Data",                         eventDate.format(AppConstants.DATE_FMT),
                "Ora",                          "09:00",
                "Quota individuale",            "25.00",
                "Data conclusiva",              eventDate.format(AppConstants.DATE_FMT)
        ));

        List<String> errori = ps.validaProposta(p);
        assertFalse(errori.isEmpty());
    }

    @Test
    @DisplayName("validaProposta with data conclusiva before data evento → error returned")
    void validaProposta_conclusivaBeforeEvento_hasError()
    {
        Proposta p = ps.creaProposta("Sport");
        LocalDate deadline   = LocalDate.now().plusDays(5);
        LocalDate eventDate  = deadline.plusDays(3);
        LocalDate conclusDate = eventDate.minusDays(1);   // before event

        p.putAllValoriCampi(Map.of(
                "Titolo",                       "Gita",
                "Numero di partecipanti",       "10",
                "Termine ultimo di iscrizione", deadline.format(AppConstants.DATE_FMT),
                "Luogo",                        "Brescia",
                "Data",                         eventDate.format(AppConstants.DATE_FMT),
                "Ora",                          "09:00",
                "Quota individuale",            "25.00",
                "Data conclusiva",              conclusDate.format(AppConstants.DATE_FMT)
        ));

        List<String> errori = ps.validaProposta(p);
        assertFalse(errori.isEmpty());
    }

    // ───────────────────── pubblicaProposta ─────────────────────

    @Test
    @DisplayName("pubblicaProposta with VALIDA proposal → state becomes APERTA")
    void pubblicaProposta_valid_aperta()
    {
        Proposta p = makeValidProposta();
        ps.pubblicaProposta(p);
        assertEquals(StatoProposta.APERTA, p.getStato());
        assertNotNull(p.getDataPubblicazione());
    }

    @Test
    @DisplayName("pubblicaProposta with non-VALIDA state → throws IllegalStateException")
    void pubblicaProposta_nonValida_throws()
    {
        Proposta p = ps.creaProposta("Sport");   // BOZZA
        assertThrows(IllegalStateException.class, () -> ps.pubblicaProposta(p));
    }

    @Test
    @DisplayName("pubblicaProposta with duplicate Titolo+Data+Ora+Luogo → throws IllegalStateException")
    void pubblicaProposta_duplicate_throws()
    {
        Proposta p1 = makeValidProposta();
        ps.pubblicaProposta(p1);

        Proposta p2 = ps.creaProposta("Sport");
        p2.putAllValoriCampi(p1.getValoriCampi());
        ps.validaProposta(p2);
        assertThrows(IllegalStateException.class, () -> ps.pubblicaProposta(p2));
    }

    // ───────────────────── getBacheca ─────────────────────

    @Test
    @DisplayName("getBacheca returns only APERTA proposals")
    void getBacheca_onlyAperta()
    {
        Proposta p = makeValidProposta();
        ps.pubblicaProposta(p);

        List<Proposta> bacheca = ps.getBacheca();
        assertEquals(1, bacheca.size());
        assertEquals(StatoProposta.APERTA, bacheca.get(0).getStato());
    }

    @Test
    @DisplayName("getBacheca excludes BOZZA and VALIDA proposals")
    void getBacheca_excludesDraftAndValid()
    {
        ps.creaProposta("Sport");       // BOZZA — not in bacheca
        makeValidProposta();            // VALIDA — not in bacheca
        assertTrue(ps.getBacheca().isEmpty());
    }

    // ───────────────────── Helper ─────────────────────

    private Proposta makeValidProposta()
    {
        Proposta p = ps.creaProposta("Sport");
        LocalDate deadline  = LocalDate.now().plusDays(5);
        LocalDate eventDate = deadline.plusDays(3);

        p.putAllValoriCampi(Map.of(
                "Titolo",                       "Gita " + System.nanoTime(),
                "Numero di partecipanti",       "10",
                "Termine ultimo di iscrizione", deadline.format(AppConstants.DATE_FMT),
                "Luogo",                        "Brescia",
                "Data",                         eventDate.format(AppConstants.DATE_FMT),
                "Ora",                          "09:00",
                "Quota individuale",            "25.00",
                "Data conclusiva",              eventDate.format(AppConstants.DATE_FMT)
        ));

        ps.validaProposta(p);
        return p;
    }
}
