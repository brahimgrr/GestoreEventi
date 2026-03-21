package it.unibs.ingsoft.v2.domain;

import java.time.format.DateTimeFormatter;

public final class AppConstants
{
    public static final String            DATE_FORMAT_LABEL = "dd/MM/yyyy";
    public static final DateTimeFormatter DATE_FMT          = DateTimeFormatter.ofPattern(DATE_FORMAT_LABEL);

    private AppConstants() {}
}
