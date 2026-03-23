package it.unibs.ingsoft.v3.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpazioPersonaleTest {

    private SpazioPersonale spazio;

    @BeforeEach
    void setUp() {
        spazio = new SpazioPersonale();
    }

    @Test
    void testAddNotifica_AggiunteCorrettamente() {
        Notifica n = new Notifica("Il tuo evento è confermato");
        spazio.addNotifica(n);

        assertEquals(1, spazio.getNotifiche().size());
        assertEquals("Il tuo evento è confermato", spazio.getNotifiche().get(0).getMessaggio());
    }

    @Test
    void testAddNotifica_DuplicatoIgnorato() {
        // Notifica.equals is id-based, so the same instance always has the same id
        Notifica n = new Notifica("msg");
        spazio.addNotifica(n);
        spazio.addNotifica(n);

        assertEquals(1, spazio.getNotifiche().size());
    }

    @Test
    void testRemoveNotifica_RimuoveCorrettamente() {
        Notifica n1 = new Notifica("Prima notifica");
        Notifica n2 = new Notifica("Seconda notifica");
        spazio.addNotifica(n1);
        spazio.addNotifica(n2);

        spazio.removeNotifica(n1);

        assertEquals(1, spazio.getNotifiche().size());
        assertEquals(n2, spazio.getNotifiche().get(0));
    }

    @Test
    void testRemoveNotifica_ElementoInesistente_NessunEffetto() {
        Notifica mai_aggiunta = new Notifica("non presente");
        assertDoesNotThrow(() -> spazio.removeNotifica(mai_aggiunta));
        assertTrue(spazio.getNotifiche().isEmpty());
    }

    @Test
    void testGetNotifiche_ReturnsUnmodifiableView() {
        assertThrows(UnsupportedOperationException.class,
                () -> spazio.getNotifiche().add(new Notifica("tentativo")));
    }
}
