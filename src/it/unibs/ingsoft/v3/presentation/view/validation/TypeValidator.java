package it.unibs.ingsoft.v3.presentation.view.validation;

import it.unibs.ingsoft.v3.domain.TipoDato;

/**
 * Validates a raw string against the expected {@link TipoDato}.
 *
 * @return an error message, or {@code null} if the value is valid for that type
 */
@FunctionalInterface
public interface TypeValidator
{
    String validate(String input, TipoDato tipo);
}
