package it.unibs.ingsoft.v1.presentation.view.contract;

/**
 * Composed view interface used by controllers.
 * Extends the fine-grained {@link IOutputView} and {@link IInputView} sub-interfaces
 * in accordance with the Interface Segregation Principle (ISP).
 *
 * <p>Any GUI implementation can implement this interface without touching controller code.</p>
 * <p>Components that only need output may depend on {@link IOutputView};
 * components that only need input may depend on {@link IInputView}.</p>
 */
public interface IAppView extends IOutputView, IInputView
{
    // All methods are inherited from the sub-interfaces.
}
