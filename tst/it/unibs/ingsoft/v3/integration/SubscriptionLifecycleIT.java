package it.unibs.ingsoft.v3.integration;

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

/**
 * Integration tests for V3 subscription lifecycle.
 * Validates the full CategoriaService ↔ PropostaService ↔ IscrizioneService
 * ↔ NotificaService interaction across the subscribe/confirm/annul workflow.
 */
@DisplayName("V3 Integration – Subscription Lifecycle")
class SubscriptionLifecycleIT
{
    private AppData data;
    private CategoriaService cs;
    private PropostaService ps;
    private IscrizioneService is;
    private NotificaService ns;
    private FruitoreService fs;

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
        fs = new FruitoreService(mockDb, data, ns);
        NotificaListener listener = (user, msg) -> ns.aggiungiNotifica(user, msg);
        is = new IscrizioneService(mockDb, data, listener);
        cs.createCategoria("Sport");
    }

    @Test
    @DisplayName("Subscribe user to open proposal → subscription recorded")
    void subscribeUser_toOpenProposal()
    {
        // Arrange
        Proposta p = makeOpenProposta(10);
        Fruitore fruitore = makeFruitore("mario");

        // Act
        is.iscrivi(fruitore, p);

        // Assert
        assertTrue(p.isIscrittoFruitore("mario"));
        assertEquals(1, p.getNumeroIscritti());
    }

    @Test
    @DisplayName("Two different users subscribe → both are recorded independently")
    void twoUsers_subscribeIndependently()
    {
        // Arrange
        Proposta p = makeOpenProposta(10);
        Fruitore luigi  = makeFruitore("luigi");
        Fruitore marco  = makeFruitore("marco");

        // Act
        is.iscrivi(luigi, p);
        is.iscrivi(marco, p);

        // Assert
        assertTrue(p.isIscrittoFruitore("luigi"));
        assertTrue(p.isIscrittoFruitore("marco"));
        assertEquals(2, p.getNumeroIscritti());
    }

    @Test
    @DisplayName("Deadline expiry with enough subscribers → proposal confirmed and subscribers notified")
    void deadlineExpiry_fullProposal_confirmed()
    {
        // Arrange — proposal for 1 person, subscribe 1 user
        Proposta p = makeOpenProposta(1);
        Fruitore fruitore = makeFruitore("anna");
        is.iscrivi(fruitore, p);

        // Simulate deadline passing
        p.setTermineIscrizione(LocalDate.now().minusDays(1));

        // Act
        is.controllaScadenzeAlAvvio();

        // Assert
        assertEquals(StatoProposta.CONFERMATA, p.getStato());
        assertFalse(ns.getNotifiche("anna").isEmpty(), "User should have received a confirmation notification");
    }

    @Test
    @DisplayName("Deadline expiry with zero subscribers → proposal annulled and subscribers notified")
    void deadlineExpiry_emptyProposal_annulled()
    {
        // Arrange — no subscriptions, deadline in the past
        Proposta p = makeOpenProposta(10);
        p.setTermineIscrizione(LocalDate.now().minusDays(1));

        // Act
        is.controllaScadenzeAlAvvio();

        // Assert
        assertEquals(StatoProposta.ANNULLATA, p.getStato());
    }

    @Test
    @DisplayName("Fruitore registered via FruitoreService can subscribe to a proposal")
    void registeredFruitore_canSubscribe()
    {
        // Arrange — register user through the auth service
        fs.registraFruitore("gianluca", "pass123");
        Fruitore fruitore = fs.login("gianluca", "pass123");
        assertNotNull(fruitore, "Login should succeed after registration");

        Proposta p = makeOpenProposta(5);

        // Act
        is.iscrivi(fruitore, p);

        // Assert
        assertTrue(p.isIscrittoFruitore("gianluca"));
    }

    // ───────────────────── Helpers ─────────────────────

    private Fruitore makeFruitore(String username)
    {
        return new Fruitore(username);
    }

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
