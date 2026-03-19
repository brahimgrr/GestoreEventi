package it.unibs.ingsoft.v2.domain;

import java.time.format.DateTimeFormatter;

public final class AppConstants
{
    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private AppConstants() {}
}
