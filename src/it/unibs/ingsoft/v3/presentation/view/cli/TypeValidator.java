package it.unibs.ingsoft.v3.presentation.view.cli;

import it.unibs.ingsoft.v3.domain.TipoDato;

/**
 * Strategy for validating a raw string value against a {@link TipoDato}.
 * Returns {@code null} if the value is valid, or an Italian error message otherwise.
 */
@FunctionalInterface
public interface TypeValidator
{
    String validate(String value, TipoDato tipo);
}
