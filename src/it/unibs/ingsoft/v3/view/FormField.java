package it.unibs.ingsoft.v3.view;

import it.unibs.ingsoft.v3.model.TipoDato;

import java.util.List;

/**
 * Holds the metadata for a single form field: name, label, data type,
 * whether it is mandatory, the current/previous value (for [attuale:] display),
 * and any business-rule validators to run after type validation.
 */
public final class FormField
{
    private final String             name;
    private final String             label;
    private final TipoDato           tipo;
    private final boolean            mandatory;
    private final String             currentValue;
    private final List<FieldValidator> validators;

    public FormField(String name, String label, TipoDato tipo,
                     boolean mandatory, String currentValue,
                     List<FieldValidator> validators)
    {
        this.name         = name;
        this.label        = label;
        this.tipo         = tipo;
        this.mandatory    = mandatory;
        this.currentValue = currentValue;
        this.validators   = validators == null ? List.of() : List.copyOf(validators);
    }

    public String             getName()         { return name; }
    public String             getLabel()        { return label; }
    public TipoDato           getTipo()         { return tipo; }
    public boolean            isMandatory()     { return mandatory; }
    public String             getCurrentValue() { return currentValue; }
    public List<FieldValidator> getValidators() { return validators; }
}
