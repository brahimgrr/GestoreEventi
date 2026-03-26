package it.unibs.ingsoft.v1.presentation.view.contract;

/**
 * Composed view interface used by controllers.
 * Components needing only output depend on {@link IOutputView};
 * components needing only input depend on {@link IInputView}.
 */
public interface IAppView extends IOutputView, IInputView {
    // All methods are inherited from the sub-interfaces.
}
