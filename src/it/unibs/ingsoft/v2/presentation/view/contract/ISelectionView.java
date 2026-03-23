package it.unibs.ingsoft.v2.presentation.view.contract;

import it.unibs.ingsoft.v2.domain.Categoria;

import java.util.List;
import java.util.OptionalInt;

public interface ISelectionView
{
    /**
     * Shows a numbered list of categories and returns the 0-based index of the
     * selected one, or empty if the user cancels.
     */
    OptionalInt selezionaCategoria(List<Categoria> categorie);
}
