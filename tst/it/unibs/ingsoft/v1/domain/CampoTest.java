package it.unibs.ingsoft.v1.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V1 – Campo")
class CampoTest
{
    // ───────────────────── Construction ─────────────────────

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
    @DisplayName("3-arg constructor defaults tipoDato to STRINGA")
    void constructor_threeArgs_defaultsTipoDato()
    {
        Campo c = new Campo("Titolo", TipoCampo.BASE, true);

        assertEquals(TipoDato.STRINGA, c.getTipoDato());
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
                () -> new Campo("Titolo", null, TipoDato.STRINGA, true));
    }

    // ───────────────────── withObbligatorio ─────────────────────

    @Test
    @DisplayName("withObbligatorio returns new Campo with toggled flag")
    void withObbligatorio_toggle_returnsNewInstance()
    {
        Campo c = new Campo("Note", TipoCampo.COMUNE, TipoDato.STRINGA, false);
        assertFalse(c.isObbligatorio());

        Campo updated = c.withObbligatorio(true);
        assertTrue(updated.isObbligatorio());
        assertFalse(c.isObbligatorio()); // original unchanged
        assertEquals(c.getNome(), updated.getNome());
        assertEquals(c.getTipoDato(), updated.getTipoDato());
    }

    // ───────────────────── equals / hashCode ─────────────────────

    @Test
    @DisplayName("equals is case-insensitive on name")
    void equals_sameNameDifferentCase_returnsTrue()
    {
        Campo a = new Campo("Titolo", TipoCampo.BASE, true);
        Campo b = new Campo("titolo", TipoCampo.BASE, false);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("equals ignores type — identity is name only")
    void equals_differentType_sameNameReturnsTrue()
    {
        Campo a = new Campo("Titolo", TipoCampo.BASE, true);
        Campo b = new Campo("Titolo", TipoCampo.COMUNE, true);
        assertEquals(a, b);
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
    @DisplayName("toString includes name and tipoDato")
    void toString_containsAllInfo()
    {
        Campo c = new Campo("Titolo", TipoCampo.BASE, TipoDato.STRINGA, true);
        String s = c.toString();
        assertTrue(s.contains("Titolo"));
        assertTrue(s.contains("STRINGA"));
        assertTrue(s.contains("obbligatorio"));
    }

    @Test
    @DisplayName("toString for non-mandatory field does not show obbligatorio")
    void toString_optional_noObbligatorio()
    {
        Campo c = new Campo("Note", TipoCampo.COMUNE, false);
        assertFalse(c.toString().contains("obbligatorio"));
    }
}
