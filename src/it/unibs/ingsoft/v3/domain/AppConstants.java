package it.unibs.ingsoft.v3.domain;

import java.time.format.DateTimeFormatter;

public final class AppConstants
{
    public static final String            DATE_FORMAT_LABEL = "dd/MM/yyyy";
    public static final String            TIME_FORMAT_LABEL = "HH:mm";
    public static final DateTimeFormatter DATE_FMT          = DateTimeFormatter.ofPattern(DATE_FORMAT_LABEL);
    public static final DateTimeFormatter TIME_FMT          = DateTimeFormatter.ofPattern(TIME_FORMAT_LABEL);
    private AppConstants() {}
}
