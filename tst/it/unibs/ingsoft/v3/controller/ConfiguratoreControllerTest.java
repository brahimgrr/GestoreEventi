package it.unibs.ingsoft.v3.controller;

import it.unibs.ingsoft.v3.model.*;
import it.unibs.ingsoft.v3.persistence.AppData;
import it.unibs.ingsoft.v3.persistence.IPersistenceService;
import it.unibs.ingsoft.v3.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – ConfiguratoreController (service integration)")
class ConfiguratoreControllerTest
{
    private CategoriaService cs;
    private PropostaService ps;

    @BeforeEach void setUp()
    {
        AppData data = new AppData();
        IPersistenceService db = new IPersistenceService()
        { @Override public AppData loadOrCreate() { return data; } @Override public void save(AppData x) {} };
        cs = new CategoriaService(db, data);
        ps = new PropostaService(db, data);
    }

    @Test @DisplayName("Full workflow: category → proposal → validate → publish")
    void fullWorkflow()
    {
        cs.createCategoria("Sport");
        Proposta p = ps.creaProposta("Sport");
        LocalDate dl = LocalDate.now().plusDays(5);
        LocalDate ev = dl.plusDays(3);
        p.putAllValoriCampi(Map.of("Titolo","G","Numero di partecipanti","10","Termine ultimo di iscrizione",dl.format(AppConstants.DATE_FMT),"Luogo","B","Data",ev.format(AppConstants.DATE_FMT),"Ora","09:00","Quota individuale","0","Data conclusiva",ev.format(AppConstants.DATE_FMT)));
        assertTrue(ps.validaProposta(p).isEmpty());
        ps.pubblicaProposta(p);
        assertEquals(StatoProposta.APERTA, p.getStato());
    }
}
