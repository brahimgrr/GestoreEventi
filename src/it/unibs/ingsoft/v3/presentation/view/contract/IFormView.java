package it.unibs.ingsoft.v3.presentation.view.contract;

import it.unibs.ingsoft.v3.presentation.view.cli.FormField;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Form orchestration — no domain objects, no exceptions.
 * An empty {@link Optional} means the user cancelled the form.
 * Console: delegates to {@link it.unibs.ingsoft.v3.presentation.view.cli.StepByStepFormRunner}.
 * GUI: opens a modal dialog with N text fields.
 */
public interface IFormView
{
    Optional<Map<String, String>> runForm(List<FormField> fields);
}
