package it.unibs.ingsoft.v5.view;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Form orchestration — no domain objects, no exceptions.
 * An empty {@link Optional} means the user cancelled the form.
 * Console: delegates to {@link StepByStepFormRunner}.
 * GUI: opens a modal dialog with N text fields.
 */
public interface IFormView
{
    Optional<Map<String, String>> runForm(List<FormField> fields);
}
