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

@DisplayName("V3 – FruitoreController (service integration)")
class FruitoreControllerTest
{
    private FruitoreService fruitoreService;
    private PropostaService ps;
    private CategoriaService cs;
    private IscrizioneService is;
    private AppData data;

    @BeforeEach void setUp()
    {
        data = new AppData();
        IPersistenceService db = new IPersistenceService()
        { @Override public AppData loadOrCreate() { return data; } @Override public void save(AppData x) {} };

        NotificaService ns = new NotificaService(db, data);
        fruitoreService = new FruitoreService(db, data, ns);
        cs = new CategoriaService(db, data);
        ps = new PropostaService(db, data);
        is = new IscrizioneService(db, data, fruitoreService);
    }

    @Test @DisplayName("Fruitore registers → subscribes to proposal → subscription confirmed")
    void fruitoreWorkflow()
    {
        cs.createCategoria("Sport");
        Proposta p = makeOpenProposta();
        Fruitore f = fruitoreService.registraFruitore("user1", "pass1234");
        assertNotNull(f);

        is.iscrivi(f, p);
        assertTrue(p.isIscrittoFruitore("user1"));
    }

    private Proposta makeOpenProposta()
    {
        Proposta p = ps.creaProposta("Sport");
        LocalDate dl = LocalDate.now().plusDays(30);
        LocalDate ev = dl.plusDays(3);
        p.putAllValoriCampi(Map.of("Titolo","T" + System.nanoTime(),"Numero di partecipanti","10","Termine ultimo di iscrizione",dl.format(AppConstants.DATE_FMT),"Luogo","B","Data",ev.format(AppConstants.DATE_FMT),"Ora","09:00","Quota individuale","0","Data conclusiva",ev.format(AppConstants.DATE_FMT)));
        ps.validaProposta(p);
        ps.pubblicaProposta(p);
        return p;
    }
}
