package it.unibs.ingsoft.v3.service;

import it.unibs.ingsoft.v3.model.Notifica;
import it.unibs.ingsoft.v3.persistence.AppData;
import it.unibs.ingsoft.v3.persistence.IPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – NotificaService")
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

    @Test @DisplayName("aggiungiNotifica adds notification without saving")
    void aggiungiNotifica_adds()
    {
        ns.aggiungiNotifica("user1", "Hello");
        List<Notifica> notifiche = ns.getNotifiche("user1");
        assertEquals(1, notifiche.size());
        assertEquals("Hello", notifiche.get(0).getMessaggio());
    }

    @Test @DisplayName("aggiungiNotificaESalva adds and saves")
    void aggiungiNotificaESalva_adds()
    {
        ns.aggiungiNotificaESalva("user1", "Hello");
        assertEquals(1, ns.getNotifiche("user1").size());
    }

    @Test @DisplayName("getNotifiche for user with no notifications → empty list")
    void getNotifiche_empty()
    {
        assertTrue(ns.getNotifiche("unknown").isEmpty());
    }

    @Test @DisplayName("eliminaNotifica removes by index")
    void eliminaNotifica_validIndex()
    {
        ns.aggiungiNotifica("user1", "A");
        ns.aggiungiNotifica("user1", "B");
        ns.eliminaNotifica("user1", 0);
        assertEquals(1, ns.getNotifiche("user1").size());
        assertEquals("B", ns.getNotifiche("user1").get(0).getMessaggio());
    }

    @Test @DisplayName("eliminaNotifica with out-of-bounds index → throws IndexOutOfBoundsException")
    void eliminaNotifica_outOfBounds()
    {
        ns.aggiungiNotifica("user1", "A");
        assertThrows(IndexOutOfBoundsException.class, () -> ns.eliminaNotifica("user1", 99));
    }

    @Test @DisplayName("Multiple notifications per user")
    void multipleNotifications()
    {
        ns.aggiungiNotifica("user1", "A");
        ns.aggiungiNotifica("user1", "B");
        ns.aggiungiNotifica("user1", "C");
        assertEquals(3, ns.getNotifiche("user1").size());
    }
}
