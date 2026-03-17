package it.unibs.ingsoft.v1.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V1 – Campo")
class CampoTest
{
    // ───────────────────── Construction ─────────────────────

    @Test
    @DisplayName("Constructor with valid params stores name, type, mandatory flag")
    void constructor_validParams_storesCorrectly()
    {
        Campo c = new Campo("Titolo", TipoCampo.BASE, true);

        assertEquals("Titolo", c.getNome());
        assertEquals(TipoCampo.BASE, c.getTipo());
        assertTrue(c.isObbligatorio());
    }

    @Test
    @DisplayName("Constructor trims whitespace from name")
    void constructor_nameWithSpaces_trimmed()
    {
        Campo c = new Campo("  Titolo  ", TipoCampo.BASE, true);
        assertEquals("Titolo", c.getNome());
    }

    @Test
    @DisplayName("Constructor with null name throws IllegalArgumentException")
    void constructor_nullName_throwsException()
    {
        assertThrows(IllegalArgumentException.class,
                () -> new Campo(null, TipoCampo.BASE, true));
    }

    @Test
    @DisplayName("Constructor with blank name throws IllegalArgumentException")
    void constructor_blankName_throwsException()
    {
        assertThrows(IllegalArgumentException.class,
                () -> new Campo("   ", TipoCampo.BASE, true));
    }

    @Test
    @DisplayName("Constructor with null type throws NullPointerException")
    void constructor_nullType_throwsException()
    {
        assertThrows(NullPointerException.class,
                () -> new Campo("Titolo", null, true));
    }

    // ───────────────────── Mutator ─────────────────────

    @Test
    @DisplayName("setObbligatorio toggles mandatory flag")
    void setObbligatorio_toggle_changesFlag()
    {
        Campo c = new Campo("Note", TipoCampo.COMUNE, false);
        assertFalse(c.isObbligatorio());

        c.setObbligatorio(true);
        assertTrue(c.isObbligatorio());

        c.setObbligatorio(false);
        assertFalse(c.isObbligatorio());
    }

    // ───────────────────── equals / hashCode ─────────────────────

    @Test
    @DisplayName("equals is case-insensitive on name and same type")
    void equals_sameNameDifferentCase_returnsTrue()
    {
        Campo a = new Campo("Titolo", TipoCampo.BASE, true);
        Campo b = new Campo("titolo", TipoCampo.BASE, false);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("equals returns false for different types")
    void equals_differentType_returnsFalse()
    {
        Campo a = new Campo("Titolo", TipoCampo.BASE, true);
        Campo b = new Campo("Titolo", TipoCampo.COMUNE, true);
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals returns false for different names")
    void equals_differentName_returnsFalse()
    {
        Campo a = new Campo("Titolo", TipoCampo.BASE, true);
        Campo b = new Campo("Luogo", TipoCampo.BASE, true);
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("hashCode consistent with equals")
    void hashCode_equalObjects_sameHashCode()
    {
        Campo a = new Campo("Titolo", TipoCampo.BASE, true);
        Campo b = new Campo("titolo", TipoCampo.BASE, false);
        assertEquals(a.hashCode(), b.hashCode());
    }

    // ───────────────────── toString ─────────────────────

    @Test
    @DisplayName("toString includes name, type, and mandatory info")
    void toString_containsAllInfo()
    {
        Campo c = new Campo("Titolo", TipoCampo.BASE, true);
        String s = c.toString();
        assertTrue(s.contains("Titolo"));
        assertTrue(s.contains("BASE"));
        assertTrue(s.contains("obbligatorio"));
    }

    @Test
    @DisplayName("toString shows facoltativo for non-mandatory field")
    void toString_optional_showsFacoltativo()
    {
        Campo c = new Campo("Note", TipoCampo.COMUNE, false);
        assertTrue(c.toString().contains("facoltativo"));
    }
}
