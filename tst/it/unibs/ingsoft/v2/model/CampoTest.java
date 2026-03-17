package it.unibs.ingsoft.v2.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – Campo")
class CampoTest
{
    @Test
    @DisplayName("Constructor with valid params stores name, type, tipoDato, mandatory flag")
    void constructor_validParams_storesCorrectly()
    {
        Campo c = new Campo("Titolo", TipoCampo.BASE, TipoDato.STRINGA, true);

        assertEquals("Titolo", c.getNome());
        assertEquals(TipoCampo.BASE, c.getTipo());
        assertEquals(TipoDato.STRINGA, c.getTipoDato());
        assertTrue(c.isObbligatorio());
    }

    @Test
    @DisplayName("Constructor trims whitespace from name")
    void constructor_nameWithSpaces_trimmed()
    {
        Campo c = new Campo("  Titolo  ", TipoCampo.BASE, TipoDato.STRINGA, true);
        assertEquals("Titolo", c.getNome());
    }

    @Test
    @DisplayName("Constructor with null name throws IllegalArgumentException")
    void constructor_nullName_throwsException()
    {
        assertThrows(IllegalArgumentException.class,
                () -> new Campo(null, TipoCampo.BASE, TipoDato.STRINGA, true));
    }

    @Test
    @DisplayName("Constructor with blank name throws IllegalArgumentException")
    void constructor_blankName_throwsException()
    {
        assertThrows(IllegalArgumentException.class,
                () -> new Campo("   ", TipoCampo.BASE, TipoDato.STRINGA, true));
    }

    @Test
    @DisplayName("Constructor with null TipoCampo throws NullPointerException")
    void constructor_nullTipoCampo_throwsException()
    {
        assertThrows(NullPointerException.class,
                () -> new Campo("Titolo", null, TipoDato.STRINGA, true));
    }

    @Test
    @DisplayName("Constructor with null TipoDato throws NullPointerException")
    void constructor_nullTipoDato_throwsException()
    {
        assertThrows(NullPointerException.class,
                () -> new Campo("Titolo", TipoCampo.BASE, null, true));
    }

    @Test
    @DisplayName("setObbligatorio toggles mandatory flag")
    void setObbligatorio_toggle_changesFlag()
    {
        Campo c = new Campo("Note", TipoCampo.COMUNE, TipoDato.STRINGA, false);
        assertFalse(c.isObbligatorio());
        c.setObbligatorio(true);
        assertTrue(c.isObbligatorio());
    }

    @Test
    @DisplayName("equals is case-insensitive on name and same type")
    void equals_sameNameDifferentCase_returnsTrue()
    {
        Campo a = new Campo("Titolo", TipoCampo.BASE, TipoDato.STRINGA, true);
        Campo b = new Campo("titolo", TipoCampo.BASE, TipoDato.INTERO, false);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("hashCode consistent with equals")
    void hashCode_equalObjects_sameHashCode()
    {
        Campo a = new Campo("Titolo", TipoCampo.BASE, TipoDato.STRINGA, true);
        Campo b = new Campo("titolo", TipoCampo.BASE, TipoDato.INTERO, false);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("toString includes name, type, tipoDato, and mandatory info")
    void toString_containsAllInfo()
    {
        Campo c = new Campo("Quota", TipoCampo.BASE, TipoDato.DECIMALE, true);
        String s = c.toString();
        assertTrue(s.contains("Quota"));
        assertTrue(s.contains("BASE"));
        assertTrue(s.contains("DECIMALE"));
        assertTrue(s.contains("obbligatorio"));
    }
}
