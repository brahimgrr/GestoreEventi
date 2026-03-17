package it.unibs.ingsoft.v3.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – Categoria")
class CategoriaTest
{
    @Test @DisplayName("Valid name") void valid() { assertEquals("S", new Categoria("S").getNome()); }
    @Test @DisplayName("Null name throws") void nullN() { assertThrows(IllegalArgumentException.class, () -> new Categoria(null)); }
    @Test @DisplayName("Add/remove specifico") void addRemove()
    {
        Categoria c = new Categoria("S");
        c.addCampoSpecifico(new Campo("C", TipoCampo.SPECIFICO, TipoDato.STRINGA, true));
        assertEquals(1, c.getCampiSpecifici().size());
        assertTrue(c.removeCampoSpecifico("C"));
    }
    @Test @DisplayName("equals case-insensitive") void eq() { assertEquals(new Categoria("S"), new Categoria("s")); }
}
