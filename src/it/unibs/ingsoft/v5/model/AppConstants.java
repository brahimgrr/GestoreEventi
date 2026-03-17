package it.unibs.ingsoft.v5.model;

import java.time.format.DateTimeFormatter;

public final class AppConstants
{
    private AppConstants() {}

    public static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
}
