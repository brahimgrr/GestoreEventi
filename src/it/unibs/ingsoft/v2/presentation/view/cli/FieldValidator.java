package it.unibs.ingsoft.v2.presentation.view.cli;

import java.util.Map;

/**
 * Functional interface for business-rule validation of a single field value.
 *
 * @param input the raw string value entered by the user
 * @param ctx   read-only snapshot of all values collected so far (for cross-field checks)
 * @return an error message, or {@code null} if the value is valid
 */
@FunctionalInterface
public interface FieldValidator
{
    String validate(String input, Map<String, String> ctx);
}
