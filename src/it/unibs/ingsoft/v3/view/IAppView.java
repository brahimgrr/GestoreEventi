package it.unibs.ingsoft.v3.view;

/**
 * Composed view interface used by controllers.
 * Extends the fine-grained sub-interfaces in accordance with the Interface Segregation Principle (ISP).
 */
public interface IAppView extends IOutputView, IInputView, ICompositeView
{
    // All methods are inherited from the sub-interfaces.
}
