package it.unibs.ingsoft.v4.service;

import it.unibs.ingsoft.v4.model.*;
import it.unibs.ingsoft.v4.persistence.AppData;
import it.unibs.ingsoft.v4.persistence.IPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V4 – IscrizioneService (disdici + ritira)")
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

        NotificaListener listener = (user, msg) -> { lastNotifUser = user; lastNotifMsg = msg; };
        is = new IscrizioneService(mockDb, data, listener);
    }

    @Test @DisplayName("iscrivi valid")
    void iscrivi_valid()
    {
        Proposta p = makeOpenProposta(10);
        is.iscrivi(new Fruitore("u1"), p);
        assertTrue(p.isIscrittoFruitore("u1"));
    }

    @Test @DisplayName("disdici removes subscription")
    void disdici_valid()
    {
        Proposta p = makeOpenProposta(10);
        Fruitore f = new Fruitore("u1");
        is.iscrivi(f, p);
        is.disdici(f, p);
        assertFalse(p.isIscrittoFruitore("u1"));
    }

    @Test @DisplayName("disdici on non-APERTA throws")
    void disdici_notOpen_throws()
    {
        Proposta p = makeOpenProposta(10);
        Fruitore f = new Fruitore("u1");
        is.iscrivi(f, p);
        // Force state change
        p.setStato(StatoProposta.CONFERMATA, LocalDate.now());
        assertThrows(IllegalStateException.class, () -> is.disdici(f, p));
    }

    @Test @DisplayName("disdici when not subscribed throws")
    void disdici_notSubscribed_throws()
    {
        Proposta p = makeOpenProposta(10);
        assertThrows(IllegalStateException.class, () -> is.disdici(new Fruitore("u1"), p));
    }

    @Test @DisplayName("re-subscribe after unsubscription")
    void reSubscribe_afterDisdici()
    {
        Proposta p = makeOpenProposta(10);
        Fruitore f = new Fruitore("u1");
        is.iscrivi(f, p);
        is.disdici(f, p);
        is.iscrivi(f, p);
        assertTrue(p.isIscrittoFruitore("u1"));
    }

    @Test @DisplayName("ritira on APERTA → RITIRATA + notification")
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

    @Test @DisplayName("ritira on CONFERMATA → RITIRATA")
    void ritira_confermata()
    {
        Proposta p = makeOpenProposta(1);
        Fruitore f = new Fruitore("u1");
        is.iscrivi(f, p);
        p.setStato(StatoProposta.CONFERMATA, LocalDate.now());
        is.ritira(p);
        assertEquals(StatoProposta.RITIRATA, p.getStato());
    }

    @Test @DisplayName("ritira on BOZZA throws")
    void ritira_bozza_throws()
    {
        Proposta p = ps.creaProposta("Sport");
        assertThrows(IllegalStateException.class, () -> is.ritira(p));
    }

    @Test @DisplayName("controllaScadenzeAlAvvio confirms full proposals")
    void scadenze_confirms()
    {
        Proposta p = makeOpenProposta(1);
        Fruitore f = new Fruitore("u1");
        p.addIscrizione(new Iscrizione(f, LocalDate.now())); // add directly to bypass deadline check
        data.addProposta(p);
        p.setTermineIscrizione(LocalDate.now().minusDays(3)); // expire after adding
        is.controllaScadenzeAlAvvio();
        assertEquals(StatoProposta.CONFERMATA, p.getStato());
    }

    @Test @DisplayName("controllaScadenzeAlAvvio annuls empty proposals")
    void scadenze_annuls()
    {
        Proposta p = makeOpenProposta(5);
        p.setTermineIscrizione(LocalDate.now().minusDays(1));
        data.addProposta(p);
        is.controllaScadenzeAlAvvio();
        assertEquals(StatoProposta.ANNULLATA, p.getStato());
    }

    private Proposta makeOpenProposta(int numPart)
    {
        Proposta p = ps.creaProposta("Sport");
        LocalDate dl = LocalDate.now().plusDays(30);
        LocalDate ev = dl.plusDays(3);
        p.putAllValoriCampi(Map.of(
                "Titolo", "G" + System.nanoTime(), "Numero di partecipanti", String.valueOf(numPart),
                "Termine ultimo di iscrizione", dl.format(AppConstants.DATE_FMT),
                "Luogo", "B", "Data", ev.format(AppConstants.DATE_FMT), "Ora", "09:00",
                "Quota individuale", "0", "Data conclusiva", ev.format(AppConstants.DATE_FMT)));
        ps.validaProposta(p);
        ps.pubblicaProposta(p);
        return p;
    }
}
