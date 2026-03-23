package it.unibs.ingsoft.v3.presentation.view.contract;

/**
 * Composed view interface used by controllers.
 * Components needing only output depend on {@link IOutputView};
 * components needing only input depend on {@link IInputView}.
 */
public interface IAppView extends IOutputView, IInputView
{
    /** Context hint shown at the start of every form that accepts free-text input. */
    String HINT_ANNULLA = "Digita 'annulla' per annullare.";
}
