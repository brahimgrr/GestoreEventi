package it.unibs.ingsoft.v2.integration;

import it.unibs.ingsoft.v2.application.CampoService;
import it.unibs.ingsoft.v2.application.CategoriaService;
import it.unibs.ingsoft.v2.application.PropostaService;
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

/**
 * Integration tests for V2 proposal lifecycle.
 * Validates the full CategoriaService ↔ PropostaService interaction:
 *   create category → create proposal → validate → publish → appear in bacheca.
 */
@DisplayName("V2 Integration – Proposal Lifecycle")
class PropostaLifecycleIT
{
    private CampoService     campo;
    private CategoriaService cs;
    private PropostaService  ps;

    @BeforeEach
    void setUp()
    {
        CatalogoData catalogo     = new CatalogoData();
        PropostaData proposteData = new PropostaData();

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

    @Test
    @DisplayName("Full lifecycle: BOZZA → VALIDA → APERTA → appears in bacheca")
    void fullLifecycle_bozzaToAperta()
    {
        LocalDate deadline  = LocalDate.now().plusDays(5);
        LocalDate eventDate = deadline.plusDays(3);

        Proposta p = ps.creaProposta("Sport");
        p.putAllValoriCampi(Map.of(
                "Titolo",                       "Gita in montagna",
                "Numero di partecipanti",       "20",
                "Termine ultimo di iscrizione", deadline.format(AppConstants.DATE_FMT),
                "Luogo",                        "Brescia",
                "Data",                         eventDate.format(AppConstants.DATE_FMT),
                "Ora",                          "08:00",
                "Quota individuale",            "15.00",
                "Data conclusiva",              eventDate.format(AppConstants.DATE_FMT)
        ));
        List<String> errori = ps.validaProposta(p);
        ps.pubblicaProposta(p);

        assertTrue(errori.isEmpty(), "Validation errors: " + errori);
        assertEquals(StatoProposta.APERTA, p.getStato());
        assertEquals(1, ps.getBacheca().size());
        assertSame(p, ps.getBacheca().get(0));
    }

    @Test
    @DisplayName("Bacheca reflects only published proposals; drafts are excluded")
    void bacheca_excludesDraftAndValid()
    {
        ps.creaProposta("Sport");   // left in BOZZA — should not appear

        LocalDate deadline  = LocalDate.now().plusDays(5);
        LocalDate eventDate = deadline.plusDays(3);
        Proposta aperta = ps.creaProposta("Sport");
        aperta.putAllValoriCampi(Map.of(
                "Titolo",                       "Partita di calcio",
                "Numero di partecipanti",       "10",
                "Termine ultimo di iscrizione", deadline.format(AppConstants.DATE_FMT),
                "Luogo",                        "Milano",
                "Data",                         eventDate.format(AppConstants.DATE_FMT),
                "Ora",                          "15:00",
                "Quota individuale",            "0",
                "Data conclusiva",              eventDate.format(AppConstants.DATE_FMT)
        ));
        ps.validaProposta(aperta);
        ps.pubblicaProposta(aperta);

        List<Proposta> bacheca = ps.getBacheca();
        assertEquals(1, bacheca.size());
        assertEquals(StatoProposta.APERTA, bacheca.get(0).getStato());
    }

    @Test
    @DisplayName("Duplicate proposal (same Titolo+Data+Ora+Luogo) is rejected on publish")
    void duplicateProposal_rejected()
    {
        LocalDate deadline  = LocalDate.now().plusDays(5);
        LocalDate eventDate = deadline.plusDays(3);
        Map<String, String> campi = Map.of(
                "Titolo",                       "Gita al lago",
                "Numero di partecipanti",       "8",
                "Termine ultimo di iscrizione", deadline.format(AppConstants.DATE_FMT),
                "Luogo",                        "Garda",
                "Data",                         eventDate.format(AppConstants.DATE_FMT),
                "Ora",                          "09:00",
                "Quota individuale",            "10",
                "Data conclusiva",              eventDate.format(AppConstants.DATE_FMT)
        );

        Proposta p1 = ps.creaProposta("Sport");
        p1.putAllValoriCampi(campi);
        ps.validaProposta(p1);
        ps.pubblicaProposta(p1);

        Proposta p2 = ps.creaProposta("Sport");
        p2.putAllValoriCampi(campi);
        ps.validaProposta(p2);

        assertThrows(IllegalStateException.class, () -> ps.pubblicaProposta(p2));
    }

    @Test
    @DisplayName("Proposal with specific category fields is validated correctly")
    void proposalWithSpecificFields_validatesCorrectly()
    {
        cs.addCampoSpecifico("Sport", "Certificato medico", TipoDato.BOOLEANO, false);

        LocalDate deadline  = LocalDate.now().plusDays(5);
        LocalDate eventDate = deadline.plusDays(3);

        Proposta p = ps.creaProposta("Sport");
        p.putAllValoriCampi(Map.of(
                "Titolo",                       "Corsa campestre",
                "Numero di partecipanti",       "30",
                "Termine ultimo di iscrizione", deadline.format(AppConstants.DATE_FMT),
                "Luogo",                        "Parco Nord",
                "Data",                         eventDate.format(AppConstants.DATE_FMT),
                "Ora",                          "10:00",
                "Quota individuale",            "5",
                "Data conclusiva",              eventDate.format(AppConstants.DATE_FMT)
        ));

        List<String> errori = ps.validaProposta(p);
        ps.pubblicaProposta(p);

        assertTrue(errori.isEmpty(), "Unexpected errors: " + errori);
        assertEquals(StatoProposta.APERTA, p.getStato());
    }
}
