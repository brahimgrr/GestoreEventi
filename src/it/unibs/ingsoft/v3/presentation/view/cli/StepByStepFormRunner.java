package it.unibs.ingsoft.v3.presentation.view.cli;

import it.unibs.ingsoft.v3.presentation.view.contract.IInputView;
import it.unibs.ingsoft.v3.presentation.view.contract.IOutputView;

import java.util.*;

/**
 * Orchestrates multi-field form entry one field at a time.
 *
 * <ul>
 *   <li>Shows progress as {@code [N/M] FieldLabel [tipo] (*)}</li>
 *   <li>Pre-fills fields with existing values shown as {@code [attuale: value]}</li>
 *   <li>{@code annulla} — cancels the entire form, returns {@code Optional.empty()}</li>
 *   <li>{@code indietro} — goes back to the previous field</li>
 *   <li>Blank input — keeps the existing value (or re-prompts if mandatory and not set)</li>
 * </ul>
 */
public final class StepByStepFormRunner
{
    private static final String CANCEL_KEYWORD = "annulla";
    private static final String BACK_KEYWORD   = "indietro";

    private final IInputView          input;
    private final IOutputView         output;
    private final TypeValidator       typeValidator;
    private final List<FormField>     fields;

    public StepByStepFormRunner(IInputView input, IOutputView output,
                                TypeValidator typeValidator, List<FormField> fields)
    {
        this.input         = input;
        this.output        = output;
        this.typeValidator = typeValidator;
        this.fields        = List.copyOf(fields);
    }

    /**
     * Runs the form interactively.
     *
     * @return a map of field name → value on completion, or empty on cancellation
     */
    public Optional<Map<String, String>> run()
    {
        Map<String, String> ctx = new LinkedHashMap<>();

        // Seed context with pre-existing values
        for (FormField f : fields)
            if (f.getCurrentValue() != null && !f.getCurrentValue().isBlank())
                ctx.put(f.getName(), f.getCurrentValue());

        int i = 0;
        int total = fields.size();

        while (i < total)
        {
            FormField f = fields.get(i);
            String current = ctx.get(f.getName());

            String obbLabel = f.isObbligatorio() ? "(*) " : "";
            String attualeLabel = (current != null && !current.isBlank())
                    ? " [attuale: " + current + "]"
                    : "";
            String prompt = "[" + (i + 1) + "/" + total + "] " + obbLabel
                    + f.getLabel() + " [" + f.getTipo() + "]" + attualeLabel + ": ";

            String raw = input.acquisisciStringa(prompt).trim();

            if (raw.equalsIgnoreCase(CANCEL_KEYWORD)) return Optional.empty();

            if (raw.equalsIgnoreCase(BACK_KEYWORD))
            {
                if (i > 0) i--;
                continue;
            }

            // Blank input → keep existing value
            if (raw.isBlank())
            {
                if (current != null && !current.isBlank())
                {
                    output.stampaSuccesso("  Campo invariato: " + current);
                    i++;
                }
                else if (!f.isObbligatorio())
                {
                    i++;
                }
                else
                {
                    output.stampaErrore("  Campo obbligatorio. Inserire un valore.");
                }
                continue;
            }

            // Type validation
            String typeError = typeValidator.validate(raw, f.getTipo());
            if (typeError != null)
            {
                output.stampaErrore("  " + typeError);
                continue;
            }

            // Business-rule validators (cross-field)
            String businessError = null;
            for (FieldValidator v : f.getValidators())
            {
                businessError = v.validate(raw, Collections.unmodifiableMap(ctx));
                if (businessError != null) break;
            }
            if (businessError != null)
            {
                output.stampaErrore("  " + businessError);
                continue;
            }

            ctx.put(f.getName(), raw);
            output.stampaSuccesso("  ✓");
            i++;
        }

        return Optional.of(ctx);
    }
}
