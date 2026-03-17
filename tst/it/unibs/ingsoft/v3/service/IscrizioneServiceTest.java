package it.unibs.ingsoft.v3.service;

import it.unibs.ingsoft.v3.model.*;
import it.unibs.ingsoft.v3.persistence.AppData;
import it.unibs.ingsoft.v3.persistence.IPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – IscrizioneService")
class IscrizioneServiceTest
{
    private AppData data;
    private IPersistenceService mockDb;
    private IscrizioneService is;
    private PropostaService ps;
    private CategoriaService cs;
    private String lastNotifUser;
    private String lastNotifMsg;

    @BeforeEach
    void setUp()
    {
        data = new AppData();
        mockDb = new IPersistenceService()
        {
            @Override public AppData loadOrCreate() { return data; }
            @Override public void save(AppData d) {}
        };
        cs = new CategoriaService(mockDb, data);
        ps = new PropostaService(mockDb, data);
        cs.createCategoria("Sport");

        NotificaListener listener = (user, msg) -> { lastNotifUser = user; lastNotifMsg = msg; };
        is = new IscrizioneService(mockDb, data, listener);
    }

    @Test @DisplayName("iscrivi on valid open proposal → success")
    void iscrivi_valid()
    {
        Proposta p = makeOpenProposta(10);
        Fruitore f = new Fruitore("user1");
        is.iscrivi(f, p);
        assertEquals(1, p.getNumeroIscritti());
        assertTrue(p.isIscrittoFruitore("user1"));
    }

    @Test @DisplayName("iscrivi when already subscribed → exception")
    void iscrivi_alreadySubscribed_throws()
    {
        Proposta p = makeOpenProposta(10);
        Fruitore f = new Fruitore("user1");
        is.iscrivi(f, p);
        assertThrows(IllegalStateException.class, () -> is.iscrivi(f, p));
    }

    @Test @DisplayName("iscrivi on BOZZA proposal → exception")
    void iscrivi_bozza_throws()
    {
        Proposta p = ps.creaProposta("Sport");
        Fruitore f = new Fruitore("user1");
        assertThrows(IllegalStateException.class, () -> is.iscrivi(f, p));
    }

    @Test @DisplayName("controllaScadenzeAlAvvio confirms when full capacity reached")
    void controllaScadenze_fullCapacity_confirms()
    {
        Proposta p = makeOpenProposta(1);
        Fruitore f = new Fruitore("user1");
        p.addIscrizione(new Iscrizione(f, LocalDate.now())); // add directly to bypass deadline check
        data.addProposta(p); // ensure it's in the proposals list
        p.setTermineIscrizione(LocalDate.now().minusDays(1)); // expired

        is.controllaScadenzeAlAvvio();
        assertEquals(StatoProposta.CONFERMATA, p.getStato());
    }

    @Test @DisplayName("controllaScadenzeAlAvvio annuls when underfull at deadline")
    void controllaScadenze_noSubscribers_annuls()
    {
        Proposta p = makeOpenProposta(5);
        p.setTermineIscrizione(LocalDate.now().minusDays(1)); // expired, no one subscribed
        data.addProposta(p);

        is.controllaScadenzeAlAvvio();
        assertEquals(StatoProposta.ANNULLATA, p.getStato());
    }

    // ───────────────── Helpers ─────────────────

    private Proposta makeOpenProposta(int numPartecipanti)
    {
        Proposta p = ps.creaProposta("Sport");
        LocalDate deadline = LocalDate.now().plusDays(30);
        LocalDate eventDate = deadline.plusDays(3);

        p.putAllValoriCampi(Map.of(
                "Titolo", "Gita " + System.nanoTime(),
                "Numero di partecipanti", String.valueOf(numPartecipanti),
                "Termine ultimo di iscrizione", deadline.format(AppConstants.DATE_FMT),
                "Luogo", "Brescia",
                "Data", eventDate.format(AppConstants.DATE_FMT),
                "Ora", "09:00",
                "Quota individuale", "0",
                "Data conclusiva", eventDate.format(AppConstants.DATE_FMT)
        ));

        ps.validaProposta(p);
        ps.pubblicaProposta(p);
        return p;
    }
}
