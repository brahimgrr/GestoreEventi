package it.unibs.ingsoft.v2.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – CampoBaseDefinito")
class CampoBaseDefinitoTest
{
    @Test
    @DisplayName("All 8 base fields exist")
    void allValues_exist()
    {
        assertEquals(8, CampoBaseDefinito.values().length);
    }

    @Test
    @DisplayName("fromNome returns correct enum for case-insensitive name")
    void fromNome_caseInsensitive()
    {
        assertEquals(CampoBaseDefinito.TITOLO, CampoBaseDefinito.fromNome("titolo"));
        assertEquals(CampoBaseDefinito.DATA, CampoBaseDefinito.fromNome("Data"));
    }

    @Test
    @DisplayName("fromNome with null returns null")
    void fromNome_null_returnsNull()
    {
        assertNull(CampoBaseDefinito.fromNome(null));
    }

    @Test
    @DisplayName("fromNome with unknown name returns null")
    void fromNome_unknown_returnsNull()
    {
        assertNull(CampoBaseDefinito.fromNome("Inesistente"));
    }

    @Test
    @DisplayName("isNomeFisso returns true for fixed base field")
    void isNomeFisso_fixed_true()
    {
        assertTrue(CampoBaseDefinito.isNomeFisso("Titolo"));
        assertTrue(CampoBaseDefinito.isNomeFisso("Numero di partecipanti"));
    }

    @Test
    @DisplayName("isNomeFisso returns false for non-base field")
    void isNomeFisso_nonBase_false()
    {
        assertFalse(CampoBaseDefinito.isNomeFisso("Certificato medico"));
    }

    @Test
    @DisplayName("Each base field has a nomeCampo and tipoDato")
    void eachField_hasProperties()
    {
        for (CampoBaseDefinito cbd : CampoBaseDefinito.values())
        {
            assertNotNull(cbd.getNomeCampo());
            assertFalse(cbd.getNomeCampo().isBlank());
            assertNotNull(cbd.getTipoDato());
        }
    }

    @Test
    @DisplayName("TITOLO has STRINGA type, NUMERO_PARTECIPANTI has INTERO type")
    void specificFields_correctTypes()
    {
        assertEquals(TipoDato.STRINGA, CampoBaseDefinito.TITOLO.getTipoDato());
        assertEquals(TipoDato.INTERO, CampoBaseDefinito.NUMERO_PARTECIPANTI.getTipoDato());
        assertEquals(TipoDato.DATA, CampoBaseDefinito.DATA.getTipoDato());
        assertEquals(TipoDato.DECIMALE, CampoBaseDefinito.QUOTA_INDIVIDUALE.getTipoDato());
    }
}
