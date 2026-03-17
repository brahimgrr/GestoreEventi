package it.unibs.ingsoft.v1.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the abstract Persona class, exercised through the concrete Configuratore subclass.
 */
@DisplayName("V1 – Persona (via Configuratore)")
class PersonaTest
{
    // ───────────────────── Construction ─────────────────────

    @Test
    @DisplayName("Valid username is stored and trimmed")
    void constructor_validUsername_trimmed()
    {
        Configuratore c = new Configuratore("  mario  ");
        assertEquals("mario", c.getUsername());
    }

    @Test
    @DisplayName("Null username throws IllegalArgumentException")
    void constructor_nullUsername_throwsException()
    {
        assertThrows(IllegalArgumentException.class, () -> new Configuratore(null));
    }

    @Test
    @DisplayName("Blank username throws IllegalArgumentException")
    void constructor_blankUsername_throwsException()
    {
        assertThrows(IllegalArgumentException.class, () -> new Configuratore("   "));
    }

    @Test
    @DisplayName("Empty string username throws IllegalArgumentException")
    void constructor_emptyUsername_throwsException()
    {
        assertThrows(IllegalArgumentException.class, () -> new Configuratore(""));
    }

    // ───────────────────── equals / hashCode ─────────────────────

    @Test
    @DisplayName("Same username same class → equals true")
    void equals_sameUsername_true()
    {
        assertEquals(new Configuratore("mario"), new Configuratore("mario"));
    }

    @Test
    @DisplayName("Different usernames → equals false")
    void equals_differentUsername_false()
    {
        assertNotEquals(new Configuratore("mario"), new Configuratore("luigi"));
    }

    @Test
    @DisplayName("hashCode is consistent with equals")
    void hashCode_consistency()
    {
        Configuratore a = new Configuratore("mario");
        Configuratore b = new Configuratore("mario");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("equals with null returns false")
    void equals_null_false()
    {
        assertNotEquals(null, new Configuratore("mario"));
    }

    // ───────────────────── toString ─────────────────────

    @Test
    @DisplayName("toString contains class name and username")
    void toString_containsInfo()
    {
        Configuratore c = new Configuratore("mario");
        String s = c.toString();
        assertTrue(s.contains("Configuratore"));
        assertTrue(s.contains("mario"));
    }
}
