package it.unibs.ingsoft.v3.view;

import it.unibs.ingsoft.v3.model.AppConstants;
import it.unibs.ingsoft.v3.model.TipoDato;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Default implementation of {@link TypeValidator}.
 * Logic extracted from the former {@code ConsoleUI.validaInput()} static method.
 */
public final class DefaultTypeValidator implements TypeValidator
{
    public static final DefaultTypeValidator INSTANCE = new DefaultTypeValidator();

    private DefaultTypeValidator() {}

    @Override
    public String validate(String valore, TipoDato tipo)
    {
        if (valore == null || valore.isBlank())
            return "Valore vuoto.";

        return switch (tipo)
        {
            case STRINGA -> null;

            case INTERO ->
            {
                try { Integer.parseInt(valore.trim()); yield null; }
                catch (NumberFormatException e)
                { yield "Valore non valido per tipo INTERO (es. 10)."; }
            }

            case DECIMALE ->
            {
                try { Double.parseDouble(valore.trim().replace(',', '.')); yield null; }
                catch (NumberFormatException e)
                { yield "Valore non valido per tipo DECIMALE (es. 15.50 o 15,50)."; }
            }

            case DATA ->
            {
                try { LocalDate.parse(valore.trim(), AppConstants.DATE_FMT); yield null; }
                catch (DateTimeParseException e)
                { yield "Formato data non valido. Usa gg/mm/aaaa (es. 25/12/2025)."; }
            }

            case BOOLEANO ->
            {
                String v = valore.trim().toLowerCase();
                if (v.equals("s") || v.equals("si") || v.equals("sì") ||
                        v.equals("n") || v.equals("no") ||
                        v.equals("true") || v.equals("false"))
                    yield null;
                yield "Valore non valido per tipo BOOLEANO. Usa s/n oppure true/false.";
            }
        };
    }
}
