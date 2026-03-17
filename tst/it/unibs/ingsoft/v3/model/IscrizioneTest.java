package it.unibs.ingsoft.v3.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – Iscrizione")
class IscrizioneTest
{
    @Test @DisplayName("Constructor stores fruitore and date")
    void constructor()
    {
        Fruitore f  = new Fruitore("user1");
        LocalDate d = LocalDate.now();
        Iscrizione i = new Iscrizione(f, d);

        assertEquals(f, i.getFruitore());
        assertEquals(d, i.getDataIscrizione());
    }
}
