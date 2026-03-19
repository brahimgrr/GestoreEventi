package it.unibs.ingsoft.v2.presentation.view.cli;

import it.unibs.ingsoft.v2.application.PropostaService;
import it.unibs.ingsoft.v2.domain.AppConstants;
import it.unibs.ingsoft.v2.domain.Campo;
import it.unibs.ingsoft.v2.domain.Proposta;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Assembles the list of {@link FormField}s for a proposal form,
 * attaching cross-field date validators for the three date fields.
 *
 * <p>Extracted from {@code ConfiguratoreController} to separate
 * form-assembly concerns from controller dispatch logic.</p>
 */
public final class PropostaFormBuilder
{
    private PropostaFormBuilder() {}

    /**
     * Builds the ordered list of form fields for the given proposal,
     * pre-populating each field with its current value (if any) and
     * attaching date-consistency validators where applicable.
     *
     * @param proposta the proposal whose fields are to be rendered
     * @param ps       used to resolve all fields (base + common + specific)
     * @return ordered list of {@link FormField} ready for {@code IFormView#runForm}
     */
    public static List<FormField> build(Proposta proposta, PropostaService ps)
    {
        List<FormField> fields = new ArrayList<>();
        Map<String, String> valori = proposta.getValoriCampi();

        for (Campo c : ps.getTuttiCampi(proposta))
        {
            List<FieldValidator> validators = buildValidators(c.getNome());
            fields.add(new FormField(
                    c.getNome(), c.getNome(), c.getTipoDato(),
                    c.isObbligatorio(), valori.get(c.getNome()), validators));
        }
        return fields;
    }

    // ---------------------------------------------------------------
    // Private validator factory
    // ---------------------------------------------------------------

    private static List<FieldValidator> buildValidators(String nomeCampo)
    {
        List<FieldValidator> validators = new ArrayList<>();

        if (PropostaService.CAMPO_TERMINE_ISCRIZIONE.equals(nomeCampo))
        {
            validators.add(termineIscrizioneValidator());
        }
        else if (PropostaService.CAMPO_DATA.equals(nomeCampo))
        {
            validators.add(dataEventoValidator());
        }
        else if (PropostaService.CAMPO_DATA_CONCLUSIVA.equals(nomeCampo))
        {
            validators.add(dataConclusivaValidator());
        }

        return validators;
    }

    /** Validates that the subscription deadline is strictly after today. */
    private static FieldValidator termineIscrizioneValidator()
    {
        return (input, ctx) -> {
            LocalDate d = parseQuietly(input);
            if (d == null) return null; // type error reported by the form runner
            if (!PropostaService.isTermineIscrizioneValido(d))
                return "\"" + PropostaService.CAMPO_TERMINE_ISCRIZIONE
                       + "\" deve essere successivo alla data odierna.";
            return null;
        };
    }

    /** Validates that the event date is at least 2 days after the subscription deadline. */
    private static FieldValidator dataEventoValidator()
    {
        return (input, ctx) -> {
            String refStr = ctx.get(PropostaService.CAMPO_TERMINE_ISCRIZIONE);
            if (refStr == null || refStr.isBlank()) return null;
            LocalDate termine = parseQuietly(refStr);
            LocalDate data    = parseQuietly(input);
            if (termine == null || data == null) return null;
            if (!PropostaService.isDataEventoValida(data, termine))
                return "\"" + PropostaService.CAMPO_DATA + "\" deve essere almeno 2 giorni dopo \""
                       + PropostaService.CAMPO_TERMINE_ISCRIZIONE + "\". Min: "
                       + termine.plusDays(2).format(AppConstants.DATE_FMT) + ".";
            return null;
        };
    }

    /** Validates that the concluding date is not before the event date. */
    private static FieldValidator dataConclusivaValidator()
    {
        return (input, ctx) -> {
            String refStr = ctx.get(PropostaService.CAMPO_DATA);
            if (refStr == null || refStr.isBlank()) return null;
            LocalDate data       = parseQuietly(refStr);
            LocalDate conclusiva = parseQuietly(input);
            if (data == null || conclusiva == null) return null;
            if (!PropostaService.isDataConclusivaValida(conclusiva, data))
                return "\"" + PropostaService.CAMPO_DATA_CONCLUSIVA
                       + "\" non può essere precedente a \"" + PropostaService.CAMPO_DATA + "\".";
            return null;
        };
    }

    /** Parses a date string silently; returns {@code null} on any parse failure. */
    private static LocalDate parseQuietly(String s)
    {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s.trim(), AppConstants.DATE_FMT); }
        catch (Exception e) { return null; }
    }
}
