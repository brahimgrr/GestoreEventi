package it.unibs.ingsoft.v2.controller;

import it.unibs.ingsoft.v2.model.*;
import it.unibs.ingsoft.v2.persistence.AppData;
import it.unibs.ingsoft.v2.persistence.IPersistenceService;
import it.unibs.ingsoft.v2.service.CategoriaService;
import it.unibs.ingsoft.v2.service.PropostaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – ConfiguratoreController (service integration)")
class ConfiguratoreControllerTest
{
    private CategoriaService cs;
    private PropostaService ps;

    @BeforeEach
    void setUp()
    {
        AppData data = new AppData();
        IPersistenceService mockDb = new IPersistenceService()
        {
            @Override public AppData loadOrCreate() { return data; }
            @Override public void save(AppData d) {}
        };
        cs = new CategoriaService(mockDb, data);
        ps = new PropostaService(mockDb, data);
    }

    @Test
    @DisplayName("Full V2 workflow: create category → create proposal → validate → publish")
    void fullWorkflow()
    {
        cs.createCategoria("Sport");

        Proposta p = ps.creaProposta("Sport");
        LocalDate deadline = LocalDate.now().plusDays(5);
        LocalDate eventDate = deadline.plusDays(3);

        p.putAllValoriCampi(Map.of(
                "Titolo", "Gita al lago",
                "Numero di partecipanti", "10",
                "Termine ultimo di iscrizione", deadline.format(AppConstants.DATE_FMT),
                "Luogo", "Lago",
                "Data", eventDate.format(AppConstants.DATE_FMT),
                "Ora", "09:00",
                "Quota individuale", "0",
                "Data conclusiva", eventDate.format(AppConstants.DATE_FMT)
        ));

        assertTrue(ps.validaProposta(p).isEmpty());
        assertEquals(StatoProposta.VALIDA, p.getStato());

        ps.pubblicaProposta(p);
        assertEquals(StatoProposta.APERTA, p.getStato());
        assertEquals(1, ps.getBacheca().size());
    }
}
