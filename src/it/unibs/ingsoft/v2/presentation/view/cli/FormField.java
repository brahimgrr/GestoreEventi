package it.unibs.ingsoft.v2.presentation.view.cli;

import it.unibs.ingsoft.v2.domain.TipoDato;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Metadata for a single form field used by {@link StepByStepFormRunner}.
 */
public final class FormField
{
    private final String               name;
    private final String               label;
    private final TipoDato             tipo;
    private final boolean              obbligatorio;
    private final String               currentValue;
    private final List<FieldValidator> validators;

    public FormField(String name, String label, TipoDato tipo,
                     boolean obbligatorio, String currentValue,
                     List<FieldValidator> validators)
    {
        this.name         = name;
        this.label        = label;
        this.tipo         = tipo;
        this.obbligatorio = obbligatorio;
        this.currentValue = currentValue;
        this.validators   = Collections.unmodifiableList(new ArrayList<>(validators));
    }

    public String               getName()        { return name; }
    public String               getLabel()       { return label; }
    public TipoDato             getTipo()        { return tipo; }
    public boolean              isObbligatorio() { return obbligatorio; }
    public String               getCurrentValue(){ return currentValue; }
    public List<FieldValidator> getValidators()  { return validators; }
}
