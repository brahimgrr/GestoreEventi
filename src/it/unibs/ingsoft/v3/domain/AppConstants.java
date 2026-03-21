package it.unibs.ingsoft.v3.domain;

import java.time.format.DateTimeFormatter;

/**
 * Application-wide constants shared across layers.
 * Placing these here avoids view→service DIP violations.
 */
public final class AppConstants
{
    private AppConstants() {}

    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Reserved username for the default configuratore account. */
    public static final String USERNAME_PREDEFINITO = "config";
}
