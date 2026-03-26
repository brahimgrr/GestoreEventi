package it.unibs.ingsoft.v2.domain;

/**
 * Validates a raw string against the expected {@link TipoDato}.
 *
 * @return an error message, or {@code null} if the value is valid for that type
 */
@FunctionalInterface
public interface TypeValidator {
    String validate(String input, TipoDato tipo);
}
