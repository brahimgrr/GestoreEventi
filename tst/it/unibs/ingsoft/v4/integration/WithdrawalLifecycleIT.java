package it.unibs.ingsoft.v4.integration;

import it.unibs.ingsoft.v4.model.*;
import it.unibs.ingsoft.v4.persistence.AppData;
import it.unibs.ingsoft.v4.persistence.IPersistenceService;
import it.unibs.ingsoft.v4.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for V4 withdrawal (ritira) and unsubscription (disdici) lifecycle.
 * Validates the full CategoriaService ↔ PropostaService ↔ IscrizioneService
 * ↔ NotificaService interaction for the V4-specific withdrawal flow.
 */
@DisplayName("V4 Integration – Withdrawal Lifecycle")
class WithdrawalLifecycleIT
{
    private AppData data;
    private CategoriaService cs;
    private PropostaService ps;
    private IscrizioneService is;
    private NotificaService ns;
    private final List<String> capturedNotifUsers = new ArrayList<>();
    private final List<String> capturedNotifMsgs  = new ArrayList<>();

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
        ns = new NotificaService(mockDb, data);
        NotificaListener listener = (user, msg) ->
        {
            capturedNotifUsers.add(user);
            capturedNotifMsgs.add(msg);
            ns.aggiungiNotifica(user, msg);
        };
        is = new IscrizioneService(mockDb, data, listener);
        cs.createCategoria("Sport");
    }

    @Test
    @DisplayName("Withdraw open proposal → state RITIRATA, all subscribers notified")
    void withdrawOpenProposal_notifiesAllSubscribers()
    {
        // Arrange — publish proposal, subscribe 2 users
        Proposta p = makeOpenProposta(10);
        Fruitore u1 = new Fruitore("alice");
        Fruitore u2 = new Fruitore("bob");
        is.iscrivi(u1, p);
        is.iscrivi(u2, p);

        // Act
        is.ritira(p);

        // Assert
        assertEquals(StatoProposta.RITIRATA, p.getStato());
        assertTrue(capturedNotifUsers.contains("alice"), "alice should be notified");
        assertTrue(capturedNotifUsers.contains("bob"),   "bob should be notified");
        assertTrue(capturedNotifMsgs.stream().anyMatch(m -> m.contains("RITIRATA")));
    }

    @Test
    @DisplayName("Withdraw confirmed proposal → state RITIRATA, subscribers notified")
    void withdrawConfirmedProposal_notifiesSubscribers()
    {
        // Arrange — publish, subscribe, manually confirm
        Proposta p = makeOpenProposta(1);
        Fruitore u1 = new Fruitore("carlo");
        is.iscrivi(u1, p);
        p.setStato(StatoProposta.CONFERMATA, LocalDate.now());

        // Act
        is.ritira(p);

        // Assert
        assertEquals(StatoProposta.RITIRATA, p.getStato());
        assertTrue(capturedNotifUsers.contains("carlo"));
        assertFalse(ns.getNotifiche("carlo").isEmpty());
    }

    @Test
    @DisplayName("disdici then re-subscribe → user appears subscribed again")
    void disdiciThenResubscribe()
    {
        // Arrange
        Proposta p = makeOpenProposta(10);
        Fruitore u = new Fruitore("diana");

        // Act
        is.iscrivi(u, p);
        is.disdici(u, p);
        is.iscrivi(u, p);

        // Assert
        assertTrue(p.isIscrittoFruitore("diana"));
        assertEquals(1, p.getNumeroIscritti());
    }

    @Test
    @DisplayName("Withdraw BOZZA proposal → throws IllegalStateException")
    void withdrawBozzaProposal_throws()
    {
        Proposta bozza = ps.creaProposta("Sport");
        assertThrows(IllegalStateException.class, () -> is.ritira(bozza));
    }

    @Test
    @DisplayName("Withdrawn proposal excluded from bacheca")
    void withdrawnProposal_excludedFromBacheca()
    {
        Proposta p = makeOpenProposta(10);
        is.iscrivi(new Fruitore("elena"), p);
        is.ritira(p);
        assertFalse(ps.getBacheca().contains(p));
        assertTrue(ps.getBacheca().isEmpty());
    }

    @Test
    @DisplayName("After withdrawal, proposals list still contains proposal in RITIRATA state")
    void withdrawnProposal_appearsInArchivio()
    {
        Proposta p = makeOpenProposta(10);
        is.ritira(p);
        assertTrue(ps.getArchivio().contains(p));
        assertEquals(StatoProposta.RITIRATA, p.getStato());
    }

    // ───────────────────── Helper ─────────────────────

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
