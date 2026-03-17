package it.unibs.ingsoft.v5.model;
import org.junit.jupiter.api.Test; import org.junit.jupiter.api.DisplayName; import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("V5 – Iscrizione") class IscrizioneTest { @Test @DisplayName("Constructor") void c() { Iscrizione i = new Iscrizione(new Fruitore("u"), LocalDate.now()); assertEquals("u", i.getFruitore().getUsername()); assertEquals(LocalDate.now(), i.getDataIscrizione()); } }
