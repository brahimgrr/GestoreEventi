package it.unibs.ingsoft.v4.service;

import it.unibs.ingsoft.v4.model.Notifica;
import it.unibs.ingsoft.v4.persistence.AppData;
import it.unibs.ingsoft.v4.persistence.IPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V4 – NotificaService")
class NotificaServiceTest
{
    private AppData data;
    private NotificaService ns;

    @BeforeEach
    void setUp()
    {
        data = new AppData();
        IPersistenceService mockDb = new IPersistenceService()
        {
            @Override public AppData loadOrCreate() { return data; }
            @Override public void save(AppData d) {}
        };
        ns = new NotificaService(mockDb, data);
    }

    @Test
    @DisplayName("aggiungiNotifica stores message without saving")
    void aggiungiNotifica_addsMessage()
    {
        ns.aggiungiNotifica("user1", "Ciao");
        List<Notifica> notifiche = ns.getNotifiche("user1");
        assertEquals(1, notifiche.size());
        assertEquals("Ciao", notifiche.get(0).getMessaggio());
    }

    @Test
    @DisplayName("aggiungiNotificaESalva adds notification and triggers persistence save")
    void aggiungiNotificaESalva_callsPersistence()
    {
        AtomicBoolean saved = new AtomicBoolean(false);
        IPersistenceService capturingDb = new IPersistenceService()
        {
            @Override public AppData loadOrCreate() { return data; }
            @Override public void save(AppData d) { saved.set(true); }
        };
        NotificaService nsCapturing = new NotificaService(capturingDb, data);

        nsCapturing.aggiungiNotificaESalva("user1", "Save me");
        assertTrue(saved.get());
        assertEquals(1, nsCapturing.getNotifiche("user1").size());
    }

    @Test
    @DisplayName("getNotifiche for unknown user → empty list")
    void getNotifiche_unknownUser_empty()
    {
        assertTrue(ns.getNotifiche("nobody").isEmpty());
    }

    @Test
    @DisplayName("eliminaNotifica removes notification by index")
    void eliminaNotifica_byIndex()
    {
        ns.aggiungiNotifica("user1", "A");
        ns.aggiungiNotifica("user1", "B");
        ns.eliminaNotifica("user1", 0);
        List<Notifica> notifiche = ns.getNotifiche("user1");
        assertEquals(1, notifiche.size());
        assertEquals("B", notifiche.get(0).getMessaggio());
    }

    @Test
    @DisplayName("eliminaNotifica with out-of-bounds index → throws IndexOutOfBoundsException")
    void eliminaNotifica_outOfBounds_throws()
    {
        ns.aggiungiNotifica("user1", "A");
        assertThrows(IndexOutOfBoundsException.class, () -> ns.eliminaNotifica("user1", 99));
    }

    @Test
    @DisplayName("multiple notifications accumulate correctly for one user")
    void multipleNotifications_sameUser()
    {
        ns.aggiungiNotifica("user1", "A");
        ns.aggiungiNotifica("user1", "B");
        ns.aggiungiNotifica("user1", "C");
        assertEquals(3, ns.getNotifiche("user1").size());
    }
}
