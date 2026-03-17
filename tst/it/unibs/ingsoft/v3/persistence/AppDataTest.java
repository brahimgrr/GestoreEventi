package it.unibs.ingsoft.v3.persistence;

import it.unibs.ingsoft.v3.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – AppData")
class AppDataTest
{
    private AppData data;
    @BeforeEach void setUp() { data = new AppData(); }

    @Test @DisplayName("Fruitori add/get") void fruitori() { data.addFruitore("u", "p"); assertEquals(1, data.getFruitori().size()); }
    @Test @DisplayName("Notifiche add/get") void notifiche() { data.addNotifica("u", new Notifica("Hi", java.time.LocalDate.now())); assertEquals(1, data.getNotifiche().get("u").size()); }
    @Test @DisplayName("Configuratori") void conf() { data.addConfiguratore("c", "p"); assertEquals(1, data.getConfiguratori().size()); }
    @Test @DisplayName("Proposte") void prop() { data.addProposta(new Proposta(new Categoria("S"))); assertEquals(1, data.getProposte().size()); }
    @Test @DisplayName("Categorie sorted") void cat()
    {
        data.addCategoria(new Categoria("Z")); data.addCategoria(new Categoria("A"));
        assertEquals("A", data.getCategorie().get(0).getNome());
    }
}
