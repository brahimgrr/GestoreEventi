package it.unibs.ingsoft.v1.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V1 – Categoria")
class CategoriaTest
{
    private Categoria cat;

    @BeforeEach
    void setUp()
    {
        cat = new Categoria("Sport");
    }

    // ───────────────────── Construction ─────────────────────

    @Test
    @DisplayName("Constructor with valid name")
    void constructor_validName_storesCorrectly()
    {
        assertEquals("Sport", cat.getNome());
        assertTrue(cat.getCampiSpecifici().isEmpty());
    }

    @Test
    @DisplayName("Constructor trims name")
    void constructor_nameWithSpaces_trimmed()
    {
        Categoria c = new Categoria("  Sport  ");
        assertEquals("Sport", c.getNome());
    }

    @Test
    @DisplayName("Constructor with null name throws exception")
    void constructor_nullName_throwsException()
    {
        assertThrows(IllegalArgumentException.class, () -> new Categoria(null));
    }

    @Test
    @DisplayName("Constructor with blank name throws exception")
    void constructor_blankName_throwsException()
    {
        assertThrows(IllegalArgumentException.class, () -> new Categoria("   "));
    }

    // ───────────────────── Campi Specifici ─────────────────────

    @Test
    @DisplayName("addCampoSpecifico with valid SPECIFICO field")
    void addCampoSpecifico_validField_added()
    {
        Campo campo = new Campo("Certificato medico", TipoCampo.SPECIFICO, true);
        cat.addCampoSpecifico(campo);

        assertEquals(1, cat.getCampiSpecifici().size());
        assertEquals("Certificato medico", cat.getCampiSpecifici().get(0).getNome());
    }

    @Test
    @DisplayName("addCampoSpecifico with non-SPECIFICO type throws exception")
    void addCampoSpecifico_nonSpecificoType_throwsException()
    {
        Campo campo = new Campo("Titolo", TipoCampo.BASE, true);
        assertThrows(IllegalArgumentException.class, () -> cat.addCampoSpecifico(campo));
    }

    @Test
    @DisplayName("addCampoSpecifico with null throws NullPointerException")
    void addCampoSpecifico_null_throwsException()
    {
        assertThrows(NullPointerException.class, () -> cat.addCampoSpecifico(null));
    }

    @Test
    @DisplayName("addCampoSpecifico with duplicate name throws exception")
    void addCampoSpecifico_duplicateName_throwsException()
    {
        cat.addCampoSpecifico(new Campo("Certificato medico", TipoCampo.SPECIFICO, true));
        assertThrows(IllegalArgumentException.class,
                () -> cat.addCampoSpecifico(new Campo("certificato medico", TipoCampo.SPECIFICO, false)));
    }

    @Test
    @DisplayName("removeCampoSpecifico removes existing field")
    void removeCampoSpecifico_existing_removed()
    {
        cat.addCampoSpecifico(new Campo("Certificato medico", TipoCampo.SPECIFICO, true));
        assertTrue(cat.removeCampoSpecifico("Certificato medico"));
        assertTrue(cat.getCampiSpecifici().isEmpty());
    }

    @Test
    @DisplayName("removeCampoSpecifico is case-insensitive")
    void removeCampoSpecifico_caseInsensitive_removed()
    {
        cat.addCampoSpecifico(new Campo("Certificato medico", TipoCampo.SPECIFICO, true));
        assertTrue(cat.removeCampoSpecifico("CERTIFICATO MEDICO"));
    }

    @Test
    @DisplayName("removeCampoSpecifico with non-existing name returns false")
    void removeCampoSpecifico_nonExisting_returnsFalse()
    {
        assertFalse(cat.removeCampoSpecifico("Inesistente"));
    }

    @Test
    @DisplayName("setObbligatorietaCampoSpecifico updates existing field")
    void setObbligatorietaCampoSpecifico_existing_updatesFlag()
    {
        cat.addCampoSpecifico(new Campo("Cert", TipoCampo.SPECIFICO, true));
        assertTrue(cat.setObbligatorietaCampoSpecifico("Cert", false));
        assertFalse(cat.getCampiSpecifici().get(0).isObbligatorio());
    }

    @Test
    @DisplayName("setObbligatorietaCampoSpecifico with non-existing name returns false")
    void setObbligatorietaCampoSpecifico_nonExisting_returnsFalse()
    {
        assertFalse(cat.setObbligatorietaCampoSpecifico("Inesistente", true));
    }

    @Test
    @DisplayName("containsCampo is case-insensitive")
    void containsCampo_caseInsensitive()
    {
        cat.addCampoSpecifico(new Campo("Certificato medico", TipoCampo.SPECIFICO, true));
        assertTrue(cat.containsCampo("CERTIFICATO MEDICO"));
        assertFalse(cat.containsCampo("Altro"));
    }

    @Test
    @DisplayName("getCampiSpecifici returns unmodifiable list")
    void getCampiSpecifici_returnsUnmodifiable()
    {
        assertThrows(UnsupportedOperationException.class,
                () -> cat.getCampiSpecifici().add(new Campo("x", TipoCampo.SPECIFICO, false)));
    }

    // ───────────────────── equals / hashCode ─────────────────────

    @Test
    @DisplayName("equals is case-insensitive on name")
    void equals_sameNameDifferentCase_true()
    {
        assertEquals(new Categoria("Sport"), new Categoria("sport"));
    }

    @Test
    @DisplayName("equals with different names returns false")
    void equals_differentName_false()
    {
        assertNotEquals(new Categoria("Sport"), new Categoria("Musica"));
    }

    @Test
    @DisplayName("hashCode consistent with equals")
    void hashCode_equalObjects_sameHash()
    {
        assertEquals(new Categoria("Sport").hashCode(), new Categoria("sport").hashCode());
    }
}
