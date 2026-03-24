package it.unibs.ingsoft.v4.domain;

import java.time.Clock;
import java.time.format.DateTimeFormatter;

public final class AppConstants
{
    public static final String            DATE_FORMAT_LABEL = "dd/MM/yyyy";
    public static final String            TIME_FORMAT_LABEL = "HH:mm";
    public static final DateTimeFormatter DATE_FMT          = DateTimeFormatter.ofPattern(DATE_FORMAT_LABEL);
    public static final DateTimeFormatter TIME_FMT          = DateTimeFormatter.ofPattern(TIME_FORMAT_LABEL);
    public static Clock clock = Clock.systemDefaultZone();

    // Canonical field-name constants (single source of truth, domain layer)
    public static final String CAMPO_TITOLO             = "Titolo";
    public static final String CAMPO_TERMINE_ISCRIZIONE = "Termine ultimo di iscrizione";
    public static final String CAMPO_DATA               = "Data";
    public static final String CAMPO_DATA_CONCLUSIVA    = "Data conclusiva";
    public static final String CAMPO_ORA                = "Ora";
    public static final String CAMPO_LUOGO              = "Luogo";
    public static final String CAMPO_QUOTA              = "Quota individuale";
    public static final String CAMPO_NUM_PARTECIPANTI   = "Numero di partecipanti";

    private AppConstants() {}
}
