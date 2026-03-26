package it.unibs.ingsoft.v3.presentation.view.contract;

/**
 * Thrown when the user types the cancel keyword during any string input.
 * Extends {@link OperationCancelledException} so controllers can catch
 * the contract-level exception without importing this class.
 */
public class CancelException extends OperationCancelledException {
    public CancelException() {
        super();
    }
}
