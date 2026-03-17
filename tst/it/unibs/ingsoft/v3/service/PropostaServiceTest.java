package it.unibs.ingsoft.v3.service;

import it.unibs.ingsoft.v3.model.*;
import it.unibs.ingsoft.v3.persistence.AppData;
import it.unibs.ingsoft.v3.persistence.IPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – PropostaService")
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

    @Test @DisplayName("creaProposta valid")
    void creaProposta() { assertEquals(StatoProposta.BOZZA, ps.creaProposta("Sport").getStato()); }

    @Test @DisplayName("creaProposta non-existing → throws")
    void creaProposta_notFound() { assertThrows(IllegalArgumentException.class, () -> ps.creaProposta("XXX")); }

    @Test @DisplayName("validaProposta + pubblicaProposta → APERTA")
    void validateAndPublish()
    {
        Proposta p = makeValidProposta();
        ps.pubblicaProposta(p);
        assertEquals(StatoProposta.APERTA, p.getStato());
    }

    @Test @DisplayName("getBacheca only APERTA")
    void getBacheca()
    {
        Proposta p = makeValidProposta();
        ps.pubblicaProposta(p);
        assertEquals(1, ps.getBacheca().size());
    }

    @Test @DisplayName("getProposteIscrittePerFruitore returns relevant proposals")
    void getProposteIscritte()
    {
        Proposta p = makeValidProposta();
        ps.pubblicaProposta(p);
        p.addIscrizione(new Iscrizione(new Fruitore("u1"), LocalDate.now()));

        List<Proposta> result = ps.getProposteIscrittePerFruitore("u1");
        assertEquals(1, result.size());
    }

    @Test @DisplayName("getArchivio returns all non-BOZZA/VALIDA")
    void getArchivio()
    {
        Proposta p = makeValidProposta();
        ps.pubblicaProposta(p);
        assertEquals(1, ps.getArchivio().size());
    }

    @Test @DisplayName("duplicate proposal publish → throws")
    void duplicate_throws()
    {
        Proposta p1 = makeValidProposta();
        ps.pubblicaProposta(p1);
        // Create same title/data/ora/luogo
        Proposta p2 = ps.creaProposta("Sport");
        p2.putAllValoriCampi(p1.getValoriCampi());
        ps.validaProposta(p2);
        assertThrows(IllegalStateException.class, () -> ps.pubblicaProposta(p2));
    }

    private Proposta makeValidProposta()
    {
        Proposta p = ps.creaProposta("Sport");
        LocalDate deadline = LocalDate.now().plusDays(5);
        LocalDate eventDate = deadline.plusDays(3);
        p.putAllValoriCampi(Map.of(
                "Titolo", "G" + System.nanoTime(),
                "Numero di partecipanti", "10",
                "Termine ultimo di iscrizione", deadline.format(AppConstants.DATE_FMT),
                "Luogo", "Brescia",
                "Data", eventDate.format(AppConstants.DATE_FMT),
                "Ora", "09:00",
                "Quota individuale", "0",
                "Data conclusiva", eventDate.format(AppConstants.DATE_FMT)
        ));
        ps.validaProposta(p);
        return p;
    }
}
