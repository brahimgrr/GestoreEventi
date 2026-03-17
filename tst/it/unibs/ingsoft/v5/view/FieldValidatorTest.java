package it.unibs.ingsoft.v5.view;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V5 – FieldValidator")
class FieldValidatorTest
{
    @Test @DisplayName("Valid input → null")
    void valid()
    {
        FieldValidator v = (input, ctx) -> input.isBlank() ? "campo vuoto" : null;
        assertNull(v.validate("hello", Map.of()));
    }

    @Test @DisplayName("Invalid input → error message")
    void invalid()
    {
        FieldValidator v = (input, ctx) -> input.isBlank() ? "campo vuoto" : null;
        assertEquals("campo vuoto", v.validate("", Map.of()));
    }

    @Test @DisplayName("Cross-field validation using context")
    void crossField()
    {
        FieldValidator v = (input, ctx) -> {
            String other = ctx.get("min");
            if (other != null && Integer.parseInt(input) < Integer.parseInt(other))
                return "deve essere >= min";
            return null;
        };
        assertNull(v.validate("10", Map.of("min", "5")));
        assertNotNull(v.validate("3", Map.of("min", "5")));
    }
}
