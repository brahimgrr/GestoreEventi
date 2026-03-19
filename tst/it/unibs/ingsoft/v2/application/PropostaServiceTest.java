package it.unibs.ingsoft.v2.application;

import it.unibs.ingsoft.v2.domain.*;
import it.unibs.ingsoft.v2.persistence.api.ICategoriaRepository;
import it.unibs.ingsoft.v2.persistence.api.IPropostaRepository;
import it.unibs.ingsoft.v2.persistence.dto.CatalogoData;
import it.unibs.ingsoft.v2.persistence.dto.PropostaData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – PropostaService")
class PropostaServiceTest
{
    private CatalogoData   catalogo;
    private PropostaData   proposteData;
    private CampoService   campo;
    private CategoriaService cs;
    private PropostaService  ps;

    @BeforeEach
    void setUp()
    {
        catalogo     = new CatalogoData();
        proposteData = new PropostaData();

        ICategoriaRepository catRepo = new ICategoriaRepository()
        {
            @Override public CatalogoData load()          { return catalogo; }
            @Override public void save(CatalogoData d)    {}
        };
        IPropostaRepository propRepo = new IPropostaRepository()
        {
            @Override public PropostaData load()          { return proposteData; }
            @Override public void save(PropostaData d)    {}
        };

        campo = new CampoService(catRepo, catalogo);
        cs    = new CategoriaService(catRepo, catalogo, campo);
        ps    = new PropostaService(catalogo, propRepo, proposteData);
        cs.createCategoria("Sport");
    }

    // ───────────────────── creaProposta ─────────────────────

    @Test
    @DisplayName("creaProposta with valid category → BOZZA state")
    void creaProposta_valid()
    {
        Proposta p = ps.creaProposta("Sport");
        assertEquals(StatoProposta.BOZZA, p.getStato());
        assertEquals("Sport", p.getCategoria().getNome());
    }

    @Test
    @DisplayName("creaProposta with non-existing category throws")
    void creaProposta_nonExisting_throws()
    {
        assertThrows(IllegalArgumentException.class, () -> ps.creaProposta("Inesistente"));
    }

    // ───────────────────── validaProposta ─────────────────────

    @Test
    @DisplayName("validaProposta with all mandatory fields filled and valid dates → empty errors, VALIDA state")
    void validaProposta_allValid_noErrors()
    {
        Proposta p = ps.creaProposta("Sport");

        LocalDate tomorrow  = LocalDate.now().plusDays(1);
        LocalDate eventDate = tomorrow.plusDays(3);

        p.putAllValoriCampi(Map.of(
                "Titolo",                       "Gita",
                "Numero di partecipanti",       "10",
                "Termine ultimo di iscrizione", tomorrow.format(AppConstants.DATE_FMT),
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
    @DisplayName("validaProposta with missing mandatory fields → errors returned, BOZZA state")
    void validaProposta_missingFields_hasErrors()
    {
        Proposta p = ps.creaProposta("Sport");
        List<String> errori = ps.validaProposta(p);
        assertFalse(errori.isEmpty());
        assertEquals(StatoProposta.BOZZA, p.getStato());
    }

    @Test
    @DisplayName("validaProposta with past inscription deadline → error")
    void validaProposta_pastDeadline_hasError()
    {
        Proposta p        = ps.creaProposta("Sport");
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
    @DisplayName("validaProposta with event date too close to deadline → error")
    void validaProposta_eventTooClose_hasError()
    {
        Proposta p        = ps.creaProposta("Sport");
        LocalDate deadline  = LocalDate.now().plusDays(5);
        LocalDate eventDate = deadline.plusDays(1); // only 1 day after, need >= 2

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
    @DisplayName("validaProposta with conclusive date before event date → error")
    void validaProposta_conclusiveBeforeEvent_hasError()
    {
        Proposta p        = ps.creaProposta("Sport");
        LocalDate deadline  = LocalDate.now().plusDays(5);
        LocalDate eventDate = deadline.plusDays(3);
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

    @Test
    @DisplayName("validaProposta is idempotent: calling twice on VALIDA does not throw")
    void validaProposta_idempotent()
    {
        Proposta p = makeValidProposta();
        // Call again on a VALIDA proposal — must not throw (bug B1 fix)
        assertDoesNotThrow(() -> ps.validaProposta(p));
    }

    // ───────────────────── pubblicaProposta ─────────────────────

    @Test
    @DisplayName("pubblicaProposta with valid proposal → state becomes APERTA")
    void pubblicaProposta_valid_aperta()
    {
        Proposta p = makeValidProposta();
        ps.pubblicaProposta(p);
        assertEquals(StatoProposta.APERTA, p.getStato());
        assertNotNull(p.getDataPubblicazione());
    }

    @Test
    @DisplayName("pubblicaProposta with non-VALIDA state throws")
    void pubblicaProposta_nonValida_throws()
    {
        Proposta p = ps.creaProposta("Sport");
        assertThrows(IllegalStateException.class, () -> ps.pubblicaProposta(p));
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
    @DisplayName("getBachecaPerCategoria groups by category name")
    void getBachecaPerCategoria_grouped()
    {
        Proposta p = makeValidProposta();
        ps.pubblicaProposta(p);

        Map<String, List<Proposta>> bacheca = ps.getBachecaPerCategoria();
        assertTrue(bacheca.containsKey("Sport"));
        assertEquals(1, bacheca.get("Sport").size());
    }

    // ───────────────────── getTuttiCampi ─────────────────────

    @Test
    @DisplayName("getTuttiCampi returns base + common + specific fields")
    void getTuttiCampi_all()
    {
        campo.addCampoComune("Note", TipoDato.STRINGA, false);
        cs.addCampoSpecifico("Sport", "Cert", TipoDato.BOOLEANO, true);

        Proposta p = ps.creaProposta("Sport");
        List<Campo> campi = ps.getTuttiCampi(p);

        // 8 base (from CampoBaseDefinito) + 1 common + 1 specific = 10
        assertTrue(campi.size() >= 10);
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
