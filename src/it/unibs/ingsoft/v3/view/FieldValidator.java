package it.unibs.ingsoft.v3.view;

import java.util.Map;

/**
 * Strategy interface for validating a single field value within a form context.
 * Implementations can perform cross-field checks using the accumulated context map.
 */
@FunctionalInterface
public interface FieldValidator
{
    /**
     * Validates the given input string.
     *
     * @param input   the raw string entered by the user
     * @param context the map of already-collected field values (field name → string value)
     * @return null if the value is valid, or an Italian error message string if invalid
     */
    String validate(String input, Map<String, String> context);
}
