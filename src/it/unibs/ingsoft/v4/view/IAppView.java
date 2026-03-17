package it.unibs.ingsoft.v4.view;

import it.unibs.ingsoft.v4.model.Campo;
import it.unibs.ingsoft.v4.model.Notifica;
import it.unibs.ingsoft.v4.model.Proposta;
import it.unibs.ingsoft.v4.model.TipoDato;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * View interface — defines all UI operations used by controllers.
 * Any GUI implementation replaces ConsoleUI without touching controller code.
 * Extends the ISP-compliant sub-interfaces for output, input, and compound operations.
 */
public interface IAppView extends IOutputView, IInputView, ICompositeView { }
