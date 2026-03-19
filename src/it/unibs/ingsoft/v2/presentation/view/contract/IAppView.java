package it.unibs.ingsoft.v2.presentation.view.contract;

/**
 * Composed view interface used by controllers.
 * Components needing only output depend on {@link IOutputView};
 * components needing only input depend on {@link IInputView}.
 */
public interface IAppView extends IOutputView, IInputView, IFormView, ISelectionView
{
}
