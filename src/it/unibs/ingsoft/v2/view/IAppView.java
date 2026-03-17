package it.unibs.ingsoft.v2.view;

/**
 * Composed view interface used by controllers.
 * Extends the fine-grained {@link IOutputView}, {@link IInputView} and {@link ICompositeView}
 * sub-interfaces in accordance with the Interface Segregation Principle (ISP).
 *
 * <p>Any GUI implementation can replace {@link ConsoleUI} without touching controller code.</p>
 * <p>Components that only need output may depend on {@link IOutputView};
 * components that only need input may depend on {@link IInputView};
 * components that only need compound UI forms may depend on {@link ICompositeView}.</p>
 */
public interface IAppView extends IOutputView, IInputView, ICompositeView
{
    // All methods are inherited from the sub-interfaces.
}
