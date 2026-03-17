package it.unibs.ingsoft.v4.service;

import it.unibs.ingsoft.v4.model.*;
import it.unibs.ingsoft.v4.persistence.AppData;
import it.unibs.ingsoft.v4.persistence.IPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V4 – PropostaService (full regression + ritira coverage)")
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

    // ───────────────────── creaProposta (V1/V2 regression) ─────────────────────

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

    // ───────────────────── validaProposta (V2 regression) ─────────────────────

    @Test
    @DisplayName("validaProposta with all valid fields and dates → no errors, state VALIDA")
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

        assertFalse(ps.validaProposta(p).isEmpty());
    }

    @Test
    @DisplayName("validaProposta with data conclusiva before data evento → error returned")
    void validaProposta_conclusivaBeforeEvento_hasError()
    {
        Proposta p = ps.creaProposta("Sport");
        LocalDate deadline    = LocalDate.now().plusDays(5);
        LocalDate eventDate   = deadline.plusDays(3);
        LocalDate conclusDate = eventDate.minusDays(1);

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

        assertFalse(ps.validaProposta(p).isEmpty());
    }

    // ───────────────────── pubblicaProposta (V2 regression) ─────────────────────

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
        Proposta p = ps.creaProposta("Sport");
        assertThrows(IllegalStateException.class, () -> ps.pubblicaProposta(p));
    }

    @Test
    @DisplayName("pubblicaProposta with duplicate Titolo+Data+Ora+Luogo → throws")
    void pubblicaProposta_duplicate_throws()
    {
        Proposta p1 = makeValidProposta();
        ps.pubblicaProposta(p1);

        Proposta p2 = ps.creaProposta("Sport");
        p2.putAllValoriCampi(p1.getValoriCampi());
        ps.validaProposta(p2);
        assertThrows(IllegalStateException.class, () -> ps.pubblicaProposta(p2));
    }

    // ───────────────────── getBacheca (V2 regression) ─────────────────────

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

    // ───────────────────── V3 regression (fruitore subscriptions) ─────────────────────

    @Test
    @DisplayName("getProposteIscrittePerFruitore returns proposals user is subscribed to")
    void getProposteIscritte()
    {
        Proposta p = makeValidProposta();
        ps.pubblicaProposta(p);
        p.addIscrizione(new Iscrizione(new Fruitore("u1"), LocalDate.now()));

        List<Proposta> result = ps.getProposteIscrittePerFruitore("u1");
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getArchivio returns all non-BOZZA/VALIDA proposals")
    void getArchivio()
    {
        Proposta p = makeValidProposta();
        ps.pubblicaProposta(p);
        assertEquals(1, ps.getArchivio().size());
    }

    // ───────────────────── V4 new: ritira coverage ─────────────────────

    @Test
    @DisplayName("getBacheca excludes RITIRATA proposals")
    void getBacheca_excludesRitirata()
    {
        Proposta p = makeValidProposta();
        ps.pubblicaProposta(p);
        // Simulate withdrawal: transition to RITIRATA
        p.setStato(StatoProposta.RITIRATA, LocalDate.now());
        assertTrue(ps.getBacheca().isEmpty());
    }

    @Test
    @DisplayName("getProposteRitirabili returns APERTA and CONFERMATA proposals")
    void getProposteRitirabili_includesApertaAndConfermata()
    {
        Proposta aperta = makeValidProposta();
        ps.pubblicaProposta(aperta);

        Proposta confermata = makeValidProposta();
        ps.pubblicaProposta(confermata);
        confermata.setStato(StatoProposta.CONFERMATA, LocalDate.now());

        List<Proposta> ritirabili = ps.getProposteRitirabili();
        assertEquals(2, ritirabili.size());
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
