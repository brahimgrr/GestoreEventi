package it.unibs.ingsoft.v2.presentation.view.contract;

import it.unibs.ingsoft.v1.presentation.view.contract.OperationCancelledException;

/**
 * Thrown when the user types the cancel keyword during any string input.
 * Extends {@link it.unibs.ingsoft.v1.presentation.view.contract.OperationCancelledException} so controllers can catch
 * the contract-level exception without importing this class.
 */
public class CancelException extends OperationCancelledException
{
  public CancelException() { super(); }
}