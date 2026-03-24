package it.unibs.ingsoft.v5.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultTypeValidatorTest {

    @Test
    void validate_acceptsSupportedFormatsUsedByBatchImport() {
        assertNull(DefaultTypeValidator.INSTANCE.validate("42", TipoDato.INTERO));
        assertNull(DefaultTypeValidator.INSTANCE.validate("12,50", TipoDato.DECIMALE));
        assertNull(DefaultTypeValidator.INSTANCE.validate("25/05/2026", TipoDato.DATA));
        assertNull(DefaultTypeValidator.INSTANCE.validate("18:30", TipoDato.ORA));
        assertNull(DefaultTypeValidator.INSTANCE.validate("si", TipoDato.BOOLEANO));
        assertNull(DefaultTypeValidator.INSTANCE.validate("no", TipoDato.BOOLEANO));
    }

    @Test
    void validate_rejectsMalformedValuesWithFieldSpecificMessages() {
        String integerError = DefaultTypeValidator.INSTANCE.validate("4.2", TipoDato.INTERO);
        String dateError = DefaultTypeValidator.INSTANCE.validate("2026-05-25", TipoDato.DATA);
        String timeError = DefaultTypeValidator.INSTANCE.validate("6pm", TipoDato.ORA);
        String booleanError = DefaultTypeValidator.INSTANCE.validate("maybe", TipoDato.BOOLEANO);

        assertNotNull(integerError);
        assertTrue(integerError.contains("numero intero"));
        assertTrue(dateError.contains(AppConstants.DATE_FORMAT_LABEL));
        assertTrue(timeError.contains("hh:mm"));
        assertTrue(booleanError.contains("s/si"));
    }
}
