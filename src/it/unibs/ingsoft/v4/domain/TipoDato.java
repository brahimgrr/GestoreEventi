package it.unibs.ingsoft.v4.domain;

/**
 * Data type of a field value.
 * Used to describe the expected format of field content
 * and to validate user input in later versions.
 */
public enum TipoDato {
    STRINGA,
    INTERO,
    DECIMALE,
    DATA,
    ORA,
    BOOLEANO
}
