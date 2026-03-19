package it.unibs.ingsoft.v2.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – Categoria")
class CategoriaTest
{
    @Test
    @DisplayName("Constructor with valid name")
    void constructor_validName() { assertEquals("Sport", new Categoria("Sport").getNome()); }

    @Test
    @DisplayName("Constructor with null name throws")
    void constructor_null_throws() { assertThrows(IllegalArgumentException.class, () -> new Categoria(null)); }

    @Test
    @DisplayName("addCampoSpecifico with valid SPECIFICO field")
    void addCampoSpecifico_valid()
    {
        Categoria cat = new Categoria("Sport");
        Campo c = new Campo("Cert", TipoCampo.SPECIFICO, TipoDato.BOOLEANO, true);
        cat.addCampoSpecifico(c);
        assertEquals(1, cat.getCampiSpecifici().size());
    }

    @Test
    @DisplayName("addCampoSpecifico with non-SPECIFICO type throws")
    void addCampoSpecifico_wrongType_throws()
    {
        Categoria cat = new Categoria("Sport");
        assertThrows(IllegalArgumentException.class,
                () -> cat.addCampoSpecifico(new Campo("X", TipoCampo.BASE, TipoDato.STRINGA, true)));
    }

    @Test
    @DisplayName("addCampoSpecifico duplicate name throws")
    void addCampoSpecifico_duplicate_throws()
    {
        Categoria cat = new Categoria("Sport");
        cat.addCampoSpecifico(new Campo("Cert", TipoCampo.SPECIFICO, TipoDato.BOOLEANO, true));
        assertThrows(IllegalArgumentException.class,
                () -> cat.addCampoSpecifico(new Campo("cert", TipoCampo.SPECIFICO, TipoDato.BOOLEANO, false)));
    }

    @Test
    @DisplayName("equals is case-insensitive on name")
    void equals_caseInsensitive() { assertEquals(new Categoria("Sport"), new Categoria("sport")); }

    @Test
    @DisplayName("removeCampoSpecifico removes existing")
    void removeCampoSpecifico_existing()
    {
        Categoria cat = new Categoria("Sport");
        cat.addCampoSpecifico(new Campo("Cert", TipoCampo.SPECIFICO, TipoDato.BOOLEANO, true));
        assertTrue(cat.removeCampoSpecifico("Cert"));
        assertTrue(cat.getCampiSpecifici().isEmpty());
    }

    @Test
    @DisplayName("Specific fields are sorted alphabetically")
    void addCampoSpecifico_sorted()
    {
        Categoria cat = new Categoria("Sport");
        cat.addCampoSpecifico(new Campo("Zzz", TipoCampo.SPECIFICO, TipoDato.STRINGA, false));
        cat.addCampoSpecifico(new Campo("Aaa", TipoCampo.SPECIFICO, TipoDato.STRINGA, false));
        assertEquals("Aaa", cat.getCampiSpecifici().get(0).getNome());
    }
}
