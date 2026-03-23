package it.unibs.ingsoft.v3.application;

import it.unibs.ingsoft.v3.domain.Notifica;
import it.unibs.ingsoft.v3.domain.SpazioPersonale;
import it.unibs.ingsoft.v3.persistence.api.ISpazioPersonaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceIsolationTest {

    private NotificationService notificationService;
    private Map<String, SpazioPersonale> archivio;

    @BeforeEach
    void setUp() {
        archivio = new HashMap<>();
        ISpazioPersonaleRepository repo = new ISpazioPersonaleRepository() {
            @Override
            public SpazioPersonale get(String username) {
                return archivio.computeIfAbsent(username, ignored -> new SpazioPersonale());
            }

            @Override
            public void save() {
            }
        };
        notificationService = new NotificationService(repo);
    }

    @Test
    void testNotifiche_SonoIsolatePerFruitore_AncheDopoCancellazione() {
        Notifica alice = new Notifica("notifica alice");
        Notifica bob = new Notifica("notifica bob");

        notificationService.inviaNotifica("alice", alice);
        notificationService.inviaNotifica("bob", bob);

        assertEquals(1, notificationService.getNotifiche("alice").size());
        assertEquals(1, notificationService.getNotifiche("bob").size());
        assertTrue(notificationService.getNotifiche("charlie").isEmpty());

        notificationService.cancellaNotifica("alice", alice);

        assertTrue(notificationService.getNotifiche("alice").isEmpty());
        assertEquals(1, notificationService.getNotifiche("bob").size());
        assertEquals("notifica bob", notificationService.getNotifiche("bob").get(0).getMessaggio());
    }
}
