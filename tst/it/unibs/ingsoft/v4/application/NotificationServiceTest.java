package it.unibs.ingsoft.v4.application;

import it.unibs.ingsoft.v4.domain.Notifica;
import it.unibs.ingsoft.v4.domain.SpazioPersonale;
import it.unibs.ingsoft.v4.persistence.api.ISpazioPersonaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationServiceTest {

    private NotificationService notificationService;
    private Map<String, SpazioPersonale> spazi;
    private int saveCount;

    @BeforeEach
    void setUp() {
        spazi = new HashMap<>();
        saveCount = 0;

        ISpazioPersonaleRepository repo = new ISpazioPersonaleRepository() {
            @Override
            public SpazioPersonale get(String username) {
                return spazi.computeIfAbsent(username, ignored -> new SpazioPersonale());
            }

            @Override
            public void save() {
                saveCount++;
            }
        };

        notificationService = new NotificationService(repo);
    }

    @Test
    void test_inviaNotifica_validInput_addsNotificationAndPersists() {
        Notifica notifica = new Notifica("n1", "ritiro proposta", LocalDateTime.of(2025, 1, 10, 9, 0));

        notificationService.inviaNotifica("alice", notifica);

        assertEquals(1, notificationService.getNotifiche("alice").size());
        assertEquals(notifica, notificationService.getNotifiche("alice").get(0));
        assertEquals(1, saveCount);
    }

    @Test
    void test_inviaNotifica_nullUsername_doesNothing() {
        notificationService.inviaNotifica(null, new Notifica("n1", "msg", LocalDateTime.of(2025, 1, 10, 9, 0)));

        assertTrue(spazi.isEmpty());
        assertEquals(0, saveCount);
    }

    @Test
    void test_inviaNotifica_nullNotification_doesNothing() {
        notificationService.inviaNotifica("alice", null);

        assertTrue(spazi.isEmpty());
        assertEquals(0, saveCount);
    }

    @Test
    void test_getNotifiche_returnsOnlyNotificationsOfRequestedUser() {
        notificationService.inviaNotifica("alice", new Notifica("n1", "msg1", LocalDateTime.of(2025, 1, 10, 9, 0)));
        notificationService.inviaNotifica("bob", new Notifica("n2", "msg2", LocalDateTime.of(2025, 1, 10, 9, 5)));

        assertEquals(1, notificationService.getNotifiche("alice").size());
        assertEquals("msg1", notificationService.getNotifiche("alice").get(0).getMessaggio());
        assertEquals(1, notificationService.getNotifiche("bob").size());
        assertEquals("msg2", notificationService.getNotifiche("bob").get(0).getMessaggio());
    }

    @Test
    void test_cancellaNotifica_existingNotification_removesItAndPersists() {
        Notifica notifica = new Notifica("n1", "msg1", LocalDateTime.of(2025, 1, 10, 9, 0));
        notificationService.inviaNotifica("alice", notifica);
        saveCount = 0;

        notificationService.cancellaNotifica("alice", notifica);

        assertTrue(notificationService.getNotifiche("alice").isEmpty());
        assertEquals(1, saveCount);
    }
}
