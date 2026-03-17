package it.unibs.ingsoft.v5.view;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates a multi-field interactive form in the view layer.
 *
 * Features:
 * - Field-by-field entry with progress counter [N/M]
 * - INVIO (blank) = keep existing value for both mandatory and optional fields
 * - 'annulla' keyword → throws CancelException to abort the entire form
 * - 'indietro' keyword → steps back to the previous field
 * - Type validation (via ConsoleUI.validaInput) before business validators
 * - Ordered business-rule validators (FieldValidator) with cross-field context
 * - ✅/❌ feedback after each accepted/rejected value
 */
public final class StepByStepFormRunner
{
    private final IInputView    input;
    private final IOutputView   output;
    private final List<FormField> fields;

    public StepByStepFormRunner(IInputView input, IOutputView output, List<FormField> fields)
    {
        this.input  = input;
        this.output = output;
        this.fields = List.copyOf(fields);
    }

    /**
     * Runs the form interactively.
     *
     * @return an ordered map of field name → accepted string value
     * @throws ConsoleUI.CancelException if the user types 'annulla' at any field
     */
    public Map<String, String> run()
    {
        Map<String, String> context = new LinkedHashMap<>();

        // Seed with existing values so [attuale:] display works from the start
        for (FormField f : fields)
            if (f.getCurrentValue() != null && !f.getCurrentValue().isBlank())
                context.put(f.getName(), f.getCurrentValue());

        int total = fields.size();
        int i     = 0;

        while (i < total)
        {
            FormField f        = fields.get(i);
            String    esistente = context.get(f.getName());
            String    etichetta = buildEtichetta(f, esistente, i + 1, total);

            String raw = input.acquisisciStringa(etichetta + ": ");

            // ── Cancel ──────────────────────────────────────────────
            if (raw.equalsIgnoreCase(ConsoleUI.CANCEL_KEYWORD))
                throw new ConsoleUI.CancelException();

            // ── Back ─────────────────────────────────────────────────
            if (raw.equalsIgnoreCase(ConsoleUI.BACK_KEYWORD))
            {
                if (i > 0)
                    i--;
                else
                    output.stampaInfo("Sei già al primo campo.");
                continue;
            }

            // ── Enter = keep current value ────────────────────────────
            if (raw.isBlank())
            {
                if (f.isMandatory() && (esistente == null || esistente.isBlank()))
                {
                    output.stampaErrore("Campo obbligatorio. Inserisci un valore.");
                    continue;
                }
                if (esistente != null && !esistente.isBlank())
                {
                    output.stampaSuccesso("Mantenuto: " + esistente);
                    context.put(f.getName(), esistente);
                }
                i++;
                continue;
            }

            // ── Type validation ──────────────────────────────────────
            String typeError = ConsoleUI.validaInput(raw, f.getTipo());
            if (typeError != null)
            {
                output.stampaErrore(typeError);
                continue;
            }

            // ── Business-rule validators ──────────────────────────────
            boolean valid = true;
            for (FieldValidator v : f.getValidators())
            {
                String err = v.validate(raw, context);
                if (err != null)
                {
                    output.stampaErrore(err);
                    valid = false;
                    break;
                }
            }
            if (!valid) continue;

            output.stampaSuccesso("Valido");
            context.put(f.getName(), raw);
            i++;
        }

        return context;
    }

    // ── private helpers ───────────────────────────────────────────────

    private String buildEtichetta(FormField f, String esistente, int idx, int total)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("  [").append(idx).append("/").append(total).append("] ");
        sb.append(f.getLabel());
        sb.append(f.isMandatory() ? " (*)" : " (facoltativo)");
        sb.append(" [").append(f.getTipo()).append("]");
        if (esistente != null && !esistente.isBlank())
            sb.append(" [attuale: ").append(esistente).append("]");
        return sb.toString();
    }
}
