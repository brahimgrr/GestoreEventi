package it.unibs.ingsoft.v5.service;

import it.unibs.ingsoft.v5.model.*;
import it.unibs.ingsoft.v5.persistence.AppData;
import it.unibs.ingsoft.v5.persistence.IPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V5 – IscrizioneService (disdici + ritira + capacity)")
class IscrizioneServiceTest
{
    private AppData data;
    private IscrizioneService is;
    private PropostaService ps;
    private CategoriaService cs;
    private String lastNotifUser;
    private String lastNotifMsg;

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

        NotificaListener listener = (user, msg) ->
        {
            lastNotifUser = user;
            lastNotifMsg  = msg;
        };
        is = new IscrizioneService(mockDb, data, listener);
    }

    // ---------------------------------------------------------------
    // iscrivi
    // ---------------------------------------------------------------

    @Test
    @DisplayName("iscrivi on APERTA proposal → fruitore is subscribed")
    void iscrivi_valid()
    {
        Proposta p = makeOpenProposta(10);
        is.iscrivi(new Fruitore("u1"), p);
        assertTrue(p.isIscrittoFruitore("u1"));
    }

    @Test
    @DisplayName("iscrivi duplicate → throws IllegalStateException")
    void iscrivi_duplicate_throws()
    {
        Proposta p = makeOpenProposta(10);
        Fruitore f = new Fruitore("u1");
        is.iscrivi(f, p);
        assertThrows(IllegalStateException.class, () -> is.iscrivi(f, p));
    }

    @Test
    @DisplayName("iscrivi on non-APERTA (BOZZA) proposal → throws IllegalStateException")
    void iscrivi_notOpen_throws()
    {
        Proposta p = ps.creaProposta("Sport");   // remains BOZZA
        assertThrows(IllegalStateException.class, () -> is.iscrivi(new Fruitore("u1"), p));
    }

    @Test
    @DisplayName("iscrivi beyond capacity → throws IllegalStateException")
    void iscrivi_capacityExceeded_throws()
    {
        Proposta p = makeOpenProposta(1);
        is.iscrivi(new Fruitore("u1"), p);
        assertThrows(IllegalStateException.class, () -> is.iscrivi(new Fruitore("u2"), p));
    }

    // ---------------------------------------------------------------
    // disdici
    // ---------------------------------------------------------------

    @Test
    @DisplayName("disdici removes subscription")
    void disdici_valid()
    {
        Proposta p = makeOpenProposta(10);
        Fruitore f = new Fruitore("u1");
        is.iscrivi(f, p);
        is.disdici(f, p);
        assertFalse(p.isIscrittoFruitore("u1"));
    }

    @Test
    @DisplayName("disdici on non-APERTA proposal → throws IllegalStateException")
    void disdici_notOpen_throws()
    {
        Proposta p = makeOpenProposta(10);
        Fruitore f = new Fruitore("u1");
        is.iscrivi(f, p);
        p.setStato(StatoProposta.CONFERMATA, LocalDate.now());
        assertThrows(IllegalStateException.class, () -> is.disdici(f, p));
    }

    @Test
    @DisplayName("disdici when not subscribed → throws IllegalStateException")
    void disdici_notSubscribed_throws()
    {
        Proposta p = makeOpenProposta(10);
        assertThrows(IllegalStateException.class, () -> is.disdici(new Fruitore("u1"), p));
    }

    @Test
    @DisplayName("re-subscribe after disdici → succeeds")
    void reSubscribe_afterDisdici()
    {
        Proposta p = makeOpenProposta(10);
        Fruitore f = new Fruitore("u1");
        is.iscrivi(f, p);
        is.disdici(f, p);
        is.iscrivi(f, p);
        assertTrue(p.isIscrittoFruitore("u1"));
    }

    // ---------------------------------------------------------------
    // ritira
    // ---------------------------------------------------------------

    @Test
    @DisplayName("ritira on APERTA → state becomes RITIRATA and subscribers notified")
    void ritira_aperta()
    {
        Proposta p = makeOpenProposta(10);
        Fruitore f = new Fruitore("u1");
        is.iscrivi(f, p);
        is.ritira(p);
        assertEquals(StatoProposta.RITIRATA, p.getStato());
        assertEquals("u1", lastNotifUser);
        assertTrue(lastNotifMsg.contains("RITIRATA"));
    }

    @Test
    @DisplayName("ritira on CONFERMATA → state becomes RITIRATA")
    void ritira_confermata()
    {
        Proposta p = makeOpenProposta(1);
        Fruitore f = new Fruitore("u1");
        is.iscrivi(f, p);
        p.setStato(StatoProposta.CONFERMATA, LocalDate.now());
        is.ritira(p);
        assertEquals(StatoProposta.RITIRATA, p.getStato());
    }

    @Test
    @DisplayName("ritira on BOZZA → throws IllegalStateException")
    void ritira_bozza_throws()
    {
        Proposta p = ps.creaProposta("Sport");
        assertThrows(IllegalStateException.class, () -> is.ritira(p));
    }

    // ---------------------------------------------------------------
    // controllaScadenzeAlAvvio
    // ---------------------------------------------------------------

    @Test
    @DisplayName("controllaScadenzeAlAvvio confirms full proposals past deadline")
    void scadenze_confirms()
    {
        Proposta p = makeOpenProposta(1);
        p.addIscrizione(new Iscrizione(new Fruitore("u1"), LocalDate.now()));
        p.setTermineIscrizione(LocalDate.now().minusDays(1));
        is.controllaScadenzeAlAvvio();
        assertEquals(StatoProposta.CONFERMATA, p.getStato());
    }

    @Test
    @DisplayName("controllaScadenzeAlAvvio annuls empty proposals past deadline")
    void scadenze_annuls()
    {
        Proposta p = makeOpenProposta(5);
        p.setTermineIscrizione(LocalDate.now().minusDays(1));
        is.controllaScadenzeAlAvvio();
        assertEquals(StatoProposta.ANNULLATA, p.getStato());
    }

    // ---------------------------------------------------------------
    // Helper
    // ---------------------------------------------------------------

    private Proposta makeOpenProposta(int numPart)
    {
        Proposta p = ps.creaProposta("Sport");
        LocalDate dl = LocalDate.now().plusDays(30);
        LocalDate ev = dl.plusDays(3);
        p.putAllValoriCampi(Map.of(
                "Titolo",                       "G" + System.nanoTime(),
                "Numero di partecipanti",       String.valueOf(numPart),
                "Termine ultimo di iscrizione", dl.format(AppConstants.DATE_FMT),
                "Luogo",                        "Brescia",
                "Data",                         ev.format(AppConstants.DATE_FMT),
                "Ora",                          "09:00",
                "Quota individuale",            "0",
                "Data conclusiva",              ev.format(AppConstants.DATE_FMT)));
        ps.validaProposta(p);
        ps.pubblicaProposta(p);
        return p;
    }
}
