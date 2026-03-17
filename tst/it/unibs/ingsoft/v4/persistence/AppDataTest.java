package it.unibs.ingsoft.v4.persistence;

import it.unibs.ingsoft.v4.model.*;
import org.junit.jupiter.api.BeforeEach; import org.junit.jupiter.api.Test; import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("V4 – AppData") class AppDataTest { private AppData data; @BeforeEach void setUp() { data = new AppData(); }
@Test @DisplayName("Fruitori") void f() { data.addFruitore("u", "p"); assertEquals(1, data.getFruitori().size()); }
@Test @DisplayName("Notifiche") void n() { data.addNotifica("u", new Notifica("Hi", java.time.LocalDate.now())); assertEquals(1, data.getNotifiche().get("u").size()); }
@Test @DisplayName("Proposte") void p() { data.addProposta(new Proposta(new Categoria("S"))); assertEquals(1, data.getProposte().size()); }
@Test @DisplayName("Categorie sorted") void c() { data.addCategoria(new Categoria("Z")); data.addCategoria(new Categoria("A")); assertEquals("A", data.getCategorie().get(0).getNome()); } }
