package it.unibs.ingsoft.v2.controller;

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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – ConfiguratoreController (service integration)")
class ConfiguratoreControllerTest
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
    }

    @Test
    @DisplayName("Full V2 workflow: create category → create proposal → validate → publish")
    void fullWorkflow()
    {
        cs.createCategoria("Sport");

        Proposta p = ps.creaProposta("Sport");
        LocalDate deadline  = LocalDate.now().plusDays(5);
        LocalDate eventDate = deadline.plusDays(3);

        p.putAllValoriCampi(Map.of(
                "Titolo",                       "Gita al lago",
                "Numero di partecipanti",       "10",
                "Termine ultimo di iscrizione", deadline.format(AppConstants.DATE_FMT),
                "Luogo",                        "Lago",
                "Data",                         eventDate.format(AppConstants.DATE_FMT),
                "Ora",                          "09:00",
                "Quota individuale",            "0",
                "Data conclusiva",              eventDate.format(AppConstants.DATE_FMT)
        ));

        assertTrue(ps.validaProposta(p).isEmpty());
        assertEquals(StatoProposta.VALIDA, p.getStato());

        ps.pubblicaProposta(p);
        assertEquals(StatoProposta.APERTA, p.getStato());
        assertEquals(1, ps.getBacheca().size());
    }
}
