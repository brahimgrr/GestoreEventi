package it.unibs.ingsoft.v3.application;

import it.unibs.ingsoft.v3.domain.Notifica;
import it.unibs.ingsoft.v3.domain.SpazioPersonale;
import it.unibs.ingsoft.v3.persistence.api.ISpazioPersonaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    private NotificationService notificationService;
    private SpazioPersonale mockSpazio;
    
    @BeforeEach
    void setUp() {
        mockSpazio = new SpazioPersonale();
        
        ISpazioPersonaleRepository repo = new ISpazioPersonaleRepository() {
            @Override
            public SpazioPersonale get(String username) {
                return mockSpazio;
            }

            @Override
            public void save() { }
        };
        
        notificationService = new NotificationService(repo);
    }

    @Test
    void testInviaNotifica_AggiungeCorrettamente() {
        Notifica n = new Notifica("Il tuo evento è confermato");
        
        notificationService.inviaNotifica("mario", n);
        
        List<Notifica> notifiche = notificationService.getNotifiche("mario");
        assertEquals(1, notifiche.size());
        assertEquals("Il tuo evento è confermato", notifiche.get(0).getMessaggio());
    }

    @Test
    void testCancellaNotifica_RimuoveCorrettamente() {
        Notifica n1 = new Notifica("Prima notifica");
        Notifica n2 = new Notifica("Seconda notifica");
        
        notificationService.inviaNotifica("mario", n1);
        notificationService.inviaNotifica("mario", n2);
        
        assertEquals(2, notificationService.getNotifiche("mario").size());
        
        notificationService.cancellaNotifica("mario", n1);
        
        List<Notifica> rimanenti = notificationService.getNotifiche("mario");
        assertEquals(1, rimanenti.size());
        assertEquals(n2, rimanenti.get(0));
    }
}
