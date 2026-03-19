package it.unibs.ingsoft.v2.presentation.view.cli;

import it.unibs.ingsoft.v2.domain.TipoDato;

/**
 * Functional interface for type-level validation of a raw string against a {@link TipoDato}.
 *
 * @return an error message, or {@code null} if the value is valid for that type
 */
@FunctionalInterface
public interface TypeValidator
{
    String validate(String input, TipoDato tipo);
}
