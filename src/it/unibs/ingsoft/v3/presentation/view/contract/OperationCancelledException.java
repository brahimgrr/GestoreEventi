package it.unibs.ingsoft.v3.presentation.view.contract;

/**
 * Thrown when the user explicitly cancels an ongoing operation.
 * This exception lives in the view contract package so that controllers can catch it
 * without depending on any concrete view implementation (e.g., ConsoleUI).
 */
public class OperationCancelledException extends RuntimeException
{
    public OperationCancelledException()
    {
        super("Operazione annullata dall'utente.");
    }
}
