package it.unibs.ingsoft.v5.model;
import org.junit.jupiter.api.Test; import org.junit.jupiter.api.DisplayName; import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("V5 – Notifica") class NotificaTest { @Test @DisplayName("Constructor") void c() { Notifica n = new Notifica("Hi", LocalDate.now()); assertEquals("Hi", n.getMessaggio()); } }
