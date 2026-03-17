package it.unibs.ingsoft.v1.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V1 – Configuratore")
class ConfiguratoreTest
{
    @Test
    @DisplayName("Configuratore is a Persona subclass")
    void configuratore_isPersona()
    {
        Configuratore c = new Configuratore("admin");
        assertInstanceOf(Persona.class, c);
    }

    @Test
    @DisplayName("serialVersionUID is defined (Serializable)")
    void configuratore_isSerializable()
    {
        Configuratore c = new Configuratore("admin");
        assertInstanceOf(java.io.Serializable.class, c);
    }

    @Test
    @DisplayName("Two Configuratore with same username are equal")
    void equals_sameUsername_true()
    {
        assertEquals(new Configuratore("admin"), new Configuratore("admin"));
    }

    @Test
    @DisplayName("Configuratore stores username correctly")
    void getUsername_returnsCorrectValue()
    {
        Configuratore c = new Configuratore("admin");
        assertEquals("admin", c.getUsername());
    }
}
