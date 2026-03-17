package it.unibs.ingsoft.v5.view;

/**
 * View interface — defines all UI operations used by controllers.
 * Any GUI implementation replaces ConsoleUI without touching controller code.
 *
 * Composed of three focused sub-interfaces following the Interface Segregation Principle (ISP):
 * - {@link IOutputView}    pure output/display operations
 * - {@link IInputView}     pure input/acquisition operations
 * - {@link ICompositeView} compound domain-specific operations
 */
public interface IAppView extends IOutputView, IInputView, ICompositeView
{
}
