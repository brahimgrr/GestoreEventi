package it.unibs.ingsoft.v3.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

/**
 * Default implementation of {@link TypeValidator}.
 * Validates that a raw string is parseable as the given {@link TipoDato}.
 */
public final class DefaultTypeValidator implements TypeValidator {
    public static final DefaultTypeValidator INSTANCE = new DefaultTypeValidator();

    private static final Set<String> VALORI_SI = Set.of("s", "si", "sì", "true");
    private static final Set<String> VALORI_NO = Set.of("n", "no", "false");

    private DefaultTypeValidator() {
    }

    @Override
    public String validate(String input, TipoDato tipo) {
        if (input == null || input.isBlank()) return null;

        switch (tipo) {
            case STRINGA:
                return null;
            case INTERO:
                return validateIntero(input);
            case DECIMALE:
                return validateDecimale(input);
            case DATA:
                return validateData(input);
            case BOOLEANO:
                return validateBooleano(input);
            case ORA:
                return validateOra(input);
            default:
                return null;
        }
    }

    private String validateOra(String s) {
        try {
            LocalTime.parse(s.trim(), AppConstants.TIME_FMT);
            return null;
        } catch (Exception e) {
            return "Valore non valido: inserire una data nel formato hh:mm (es. 16:30).";
        }
    }

    private String validateIntero(String s) {
        try {
            Integer.parseInt(s.trim());
            return null;
        } catch (NumberFormatException e) {
            return "Valore non valido: inserire un numero intero.";
        }
    }

    private String validateDecimale(String s) {
        try {
            Double.parseDouble(s.trim().replace(',', '.'));
            return null;
        } catch (NumberFormatException e) {
            return "Valore non valido: inserire un numero decimale.";
        }
    }

    private String validateData(String s) {
        try {
            LocalDate.parse(s.trim(), AppConstants.DATE_FMT);
            return null;
        } catch (Exception e) {
            return "Valore non valido: inserire una data nel formato "
                    + AppConstants.DATE_FORMAT_LABEL + " (es. 25/12/2026).";
        }
    }

    private String validateBooleano(String s) {
        String lower = s.trim().toLowerCase();
        if (VALORI_SI.contains(lower) || VALORI_NO.contains(lower)) return null;
        return "Valore non valido: inserire s/si/sì oppure n/no.";
    }
}
