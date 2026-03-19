package it.unibs.ingsoft.v5.view;

/**
 * View interface — defines all UI operations used by controllers.
 * Any GUI implementation replaces ConsoleUI without touching controller code.
 *
 * Composed of five focused sub-interfaces following the Interface Segregation Principle (ISP):
 * - {@link IOutputView}    pure output/display operations
 * - {@link IInputView}     pure input/acquisition operations
 * - {@link IFormView}      form orchestration (Optional return, no exceptions)
 * - {@link IDisplayView}   domain-free display via view-models
 * - {@link ISelectionView} combined display + selection via view-models
 */
public interface IAppView extends IOutputView, IInputView, IFormView, IDisplayView, ISelectionView
{
}
