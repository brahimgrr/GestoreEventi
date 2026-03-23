package it.unibs.ingsoft.v3.presentation.view.contract;

import it.unibs.ingsoft.v3.presentation.view.viewmodel.CategoriaVM;

import java.util.List;
import java.util.OptionalInt;

public interface ISelectionView
{
    /**
     * Shows a numbered list of categories and returns the 0-based index of the
     * selected one, or empty if the user cancels.
     */
    OptionalInt selezionaCategoria(List<CategoriaVM> categorie);
}
