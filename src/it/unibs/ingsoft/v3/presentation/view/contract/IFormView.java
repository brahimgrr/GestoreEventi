package it.unibs.ingsoft.v3.presentation.view.contract;

import it.unibs.ingsoft.v3.presentation.view.cli.FormField;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IFormView
{
    /** Runs an interactive step-by-step form. Returns filled values, or empty on cancel. */
    Optional<Map<String, String>> runForm(List<FormField> fields);
}
