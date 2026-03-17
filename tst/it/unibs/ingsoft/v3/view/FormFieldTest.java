package it.unibs.ingsoft.v3.view;

import it.unibs.ingsoft.v3.model.TipoDato;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – FormField")
class FormFieldTest
{
    @Test @DisplayName("Constructor stores all fields")
    void constructor()
    {
        FormField ff = new FormField("name", "Label", TipoDato.STRINGA, true, "val", null);
        assertEquals("name", ff.getName());
        assertEquals("Label", ff.getLabel());
        assertEquals(TipoDato.STRINGA, ff.getTipo());
        assertTrue(ff.isMandatory());
        assertEquals("val", ff.getCurrentValue());
        assertNotNull(ff.getValidators());
        assertTrue(ff.getValidators().isEmpty());
    }

    @Test @DisplayName("Validators list is immutable copy")
    void validators_immutable()
    {
        FieldValidator v = (input, ctx) -> null;
        FormField ff = new FormField("n", "L", TipoDato.STRINGA, false, null, List.of(v));
        assertEquals(1, ff.getValidators().size());
        assertThrows(UnsupportedOperationException.class, () -> ff.getValidators().add((i, c) -> null));
    }
}
